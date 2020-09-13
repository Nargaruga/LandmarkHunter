package com.narga.landmarkhunter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.Metadata;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.here.sdk.search.CategoryQuery;
import com.here.sdk.search.Place;
import com.here.sdk.search.PlaceCategory;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

//Fragment contenente una mappa
public class MapFragment extends Fragment implements LocationListener {
    public static final String LOG_TAG = MapFragment.class.getSimpleName();
    public static final int MAX_MARKERS = 30;
    public static final int DEFAULT_ZOOM = 2000;
    public static final int DISTANCE_FOR_POINTS = 50;
    private static final long MIN_TIME = 3000;
    private static final float MIN_DIST = 10;
    private MapMarker currentLocationMarker;
    private HashMap<String, MapMarker> markers;
    private MapFragment.InfoPanel panel;
    private LocationManager locationManager;
    private MapView mapView;
    private SearchEngine searchEngine;
    private double latitude = 0, longitude = 0;
    private SharedViewModel viewModel;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ImageButton resetMapButton = view.findViewById(R.id.reset_map_button);
        panel = new InfoPanel(view);
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        //Carico la mappa
        loadMapScene();
        //Aggiungo un ClickListener al tasto di reset della mappa
        resetMapButton.setOnClickListener(v -> resetMap());
        //Aggiungo un TapListener alla mappa
        mapView.getGestures().setTapListener(this::pickMapMarker);
        //Creo un' istanza del motore di ricerca
        try {
            searchEngine = new SearchEngine();
        } catch(InstantiationErrorException e) {
            e.printStackTrace();
        }
        //Recupero un LocationManager
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null) {
            //Se la localizzazione non è attiva notifico l' utente
            if(!isLocationEnabled())
                showAlert();
        }
        //Attivo la localizzazione
        askForUpdates();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = requireActivity();

        //Inizializzo il ViewModel per l' interazione con il DB
        viewModel = new ViewModelProvider(activity).get(SharedViewModel.class);
    }

    //Riporta la mappa sulla posizione dell' utente con uno zoom standard
    public void resetMap() {
        mapView.getCamera().lookAt(new GeoCoordinates(latitude, longitude), DEFAULT_ZOOM);
    }

    @SuppressLint("MissingPermission")
    @Override
    //Chiamato quando si ricevono i permessi richiesti
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                askForUpdates();
            } else {
                Toast.makeText(requireActivity(), "Errore durante il recupero dei permessi per la geolocalizzazione.", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Attiva la localizzazione richiedendo i permessi se necessario
    private void askForUpdates() {
        //Controllo i permessi
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Ho i permessi, mi registro per ricevere gli aggiornamenti della posizione
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);
            updateLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        } else {
            //Non ho i permessi, pertanto li richiedo
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    //Carica la mappa
    private void loadMapScene() {
        //Carica una scena dall' SDK per visualizzare la mappa con un determinato stile
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, mapError -> {
            if(mapError == null) {
                mapView.getCamera().lookAt(new GeoCoordinates(latitude, longitude), DEFAULT_ZOOM);
            } else {
                Log.d(LOG_TAG, "onLoadScene failed: " + mapError.name());
            }
        });
    }

    //Cerca punti di interesse vicini
    private void getNearbyPois() {
        ArrayList<PlaceCategory> categoryList = new ArrayList<>();
        CategoryQuery query;
        SearchOptions options = new SearchOptions(LanguageCode.IT_IT, MAX_MARKERS);

        categoryList.add(new PlaceCategory(PlaceCategory.SIGHTS_AND_MUSEUMS));
        categoryList.add(new PlaceCategory(PlaceCategory.NATURAL_AND_GEOGRAPHICAL));
        query = new CategoryQuery(categoryList, new GeoCoordinates(latitude, longitude));

        searchEngine.search(query, options, (searchError, list) -> {
            if(searchError != null) {
                Toast.makeText(requireActivity(), "Errore nella ricerca: " + searchError.toString(), Toast.LENGTH_LONG).show();
            } else if(list != null) {
                //Scorro la lista di luoghi vicini
                for(int i = 0; i < list.size(); i++) {
                    Place place = list.get(i);
                    //Procedo solo se il luovo ha delle coordinate
                    if(place.getGeoCoordinates() != null) {
                        PointOfInterest poi = new PointOfInterest(place.getId(),
                                place.getTitle(),
                                place.getAddress().streetName + " " + place.getAddress().houseNumOrName,
                                Calendar.getInstance().getTime().toString(),
                                place.getGeoCoordinates().latitude,
                                place.getGeoCoordinates().longitude,
                                null);
                        boolean visited = false;
                        if(viewModel.getAllPois().getValue() != null) {
                            ArrayList<PointOfInterest> pois = (ArrayList<PointOfInterest>) viewModel.getAllPois().getValue();

                            if(!pois.contains(poi)) {
                                if(isCloseEnough(place)) {
                                    viewModel.insertPoi(poi);
                                    visited = true;
                                }
                            } else visited = true;
                        }
                        //Se ho raggiunto il numero massimo di segnalini rimuovo quello più distante
                        if(markers.size() == MAX_MARKERS)
                            removeFurthestMarker();

                        //Controllo se il segnalino è già sulla mappa
                        MapMarker marker = markers.get(place.getId());
                        if(marker != null) {
                            //Controllo se aggiornare il colore del segnalino
                            if(marker.getMetadata() != null && !Boolean.parseBoolean(marker.getMetadata().getString("visited")) && visited)
                                addMapMarker(poi, true);
                        } else
                            addMapMarker(poi, visited);
                    }
                }
            }
        });
    }

    //Cerco il segnalino più distante e lo rimuovo
    private void removeFurthestMarker() {
        GeoCoordinates currentCoordinates = new GeoCoordinates(latitude, longitude);
        MapMarker maxDistanceMarker = null;
        double maxDistance = 0;
        //Cerco il segnalino più distante
        for(MapMarker marker : markers.values()){
            double distance = marker.getCoordinates().distanceTo(currentCoordinates);
            if(distance > maxDistance){
                maxDistanceMarker = marker;
                maxDistance = distance;
            }
        }
        //Lo rimuovo
        if(maxDistanceMarker != null && maxDistanceMarker.getMetadata() != null) {
            markers.remove(maxDistanceMarker.getMetadata().getString("id"));
            mapView.getMapScene().removeMapMarker(maxDistanceMarker);
        }
    }

    //Aggiunge un marker sulla mappa alle coordinate specificate
    private void addMapMarker(PointOfInterest poi, boolean visited) {
        if(poi == null)
            return;
        GeoCoordinates coordinates = new GeoCoordinates(poi.getLatitude(), poi.getLongitude());
        String name = poi.getName(), address = poi.getAddress();
        Metadata metadata = new Metadata();
        Anchor2D anchor = new Anchor2D(0.5f, 1);
        MapImage mapImage;
        //Controllo se il luogo è stato già visitato o meno per decidere il colore del segnalino
        if(visited)
            mapImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_visited_location);
        else
            mapImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_poi_marker);
        //Creo il segnalino e aggiungo i metadati
        MapMarker marker = new MapMarker(coordinates, mapImage, anchor);
        metadata.setString("name", name);
        metadata.setString("address", address);
        metadata.setString("visited", String.valueOf(visited));
        marker.setMetadata(metadata);
        //Aggiungo il segnalino alla mappa
        mapView.getMapScene().addMapMarker(marker);
        //Aggiungo il seganlino alla lista di MapMarkers
        markers.put(poi.getId(), marker);
    }

    //Controlla se il POI in questione è abbastanza vicino da essere considerato "visitato"
    private boolean isCloseEnough(Place place) {
        if(place == null)
            return false;

        Integer distance = place.getDistanceInMeters();
        if(distance == null)
            return false;
        else
            return distance < DISTANCE_FOR_POINTS;
    }

    //Seleziona il marker toccato dall' utente
    private void pickMapMarker(final Point2D touchPoint) {
        float radiusInPixel = 2;
        mapView.pickMapItems(touchPoint, radiusInPixel, pickMapItemsResult -> {
            if(pickMapItemsResult != null) {
                List<MapMarker> markers = pickMapItemsResult.getMarkers();
                if(markers.size() > 0) {
                    MapMarker topmostMapMarker = markers.get(0);
                    Metadata metadata = topmostMapMarker.getMetadata();

                    if(metadata != null) {
                        panel.setLocName(metadata.getString("name"));
                        panel.setAddress(metadata.getString("address"));
                    } else {
                        //Placeholder in caso di dati mancanti
                        panel.setLocName("N/A");
                        panel.setAddress("N/A");
                    }
                    panel.setVisibility(View.VISIBLE);
                } else
                    panel.setVisibility(View.GONE);
            }
        });
    }

    //Restituisce true se uno dei servizi di localizzazione è attivo, false altrimenti
    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //Chiede all' utente di attivare la localizzazione
    private void showAlert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle("Geolocalizzazione richiesta")
                .setMessage("Abilita la geolocalizzazione per permettere all' app di funzionare correttamente.")
                .setPositiveButton("Impostazioni", (dialogInterface, which) -> {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                })
                .setNegativeButton("Annulla", (dialogInterface, which) -> {
                    //TODO?
                })
                .show();
    }


    @Override
    public void onPause() {
        super.onPause();

        if(mapView != null)
            mapView.onPause();
        if(locationManager != null)
            locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        askForUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    //Chiamato quando la posizione cambia
    public void onLocationChanged(@NonNull Location location) {
        updateLocation(location);
    }

    //Aggiorna la posizione
    private void updateLocation(Location location) {
        if(location == null)
            return;
        //Recupero le nuove coordinate
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        //Recupero i punti di interesse vicini
        getNearbyPois();
        //Aggiorno il marker di posizione
        updateLocationMarker();
    }

    //Sposta il segnalino che marca la posizione corrente
    public void updateLocationMarker() {
        MapImage mapImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_current_location);
        if(currentLocationMarker != null)
            mapView.getMapScene().removeMapMarker(currentLocationMarker);
        currentLocationMarker = new MapMarker(new GeoCoordinates(latitude, longitude), mapImage);
        mapView.getMapScene().addMapMarker(currentLocationMarker);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Mantiene riferimenti ai componenti del pannello informativo
    public static class InfoPanel {
        private CardView card;
        private TextView locName;
        private TextView address;

        public InfoPanel(View r) {
            if(r != null) {
                card = r.findViewById(R.id.info_panel);
                locName = r.findViewById(R.id.panel_location_name);
                address = r.findViewById(R.id.panel_address);
            }
        }

        public void setLocName(String s) {
            locName.setText(s);
        }

        public void setAddress(String s) {
            address.setText(s);
        }

        public void setVisibility(int type) {
            card.setVisibility(type);
        }
    }
}