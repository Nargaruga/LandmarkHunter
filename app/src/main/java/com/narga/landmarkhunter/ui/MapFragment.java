package com.narga.landmarkhunter.ui;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
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
import com.narga.landmarkhunter.R;
import com.narga.landmarkhunter.data.PointOfInterest;
import com.narga.landmarkhunter.database.SharedViewModel;
import com.narga.landmarkhunter.utility.BitmapHandlingTask;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//Fragment per la gestione della mappa
public class MapFragment extends Fragment implements LocationListener, LifecycleObserver {
    public static final String LOG_TAG = MapFragment.class.getSimpleName();
    public static final int MAX_MARKERS = 30; //Massimo numero di marker visualizzabili allo stesso momento
    public static final int DEFAULT_ZOOM = 2000; //Zoom di default
    public static final int FOCUSED_ZOOM = 500; //Zoom maggiore
    public static final int DISTANCE_FOR_POINTS = 30; //Distanza (in metri) massima per considerare un POI come visitato
    private static final long MIN_TIME = 3000; //Intervallo di tempo (millisecondi) minimo tra aggiornamenti della posizione
    private static final float MIN_DIST = 0; //Spostamento (in metri) minimo tra aggiornamenti della posizione
    private MapMarker currentLocationMarker; //Marker che indica la posizione attuale
    private HashMap<String, MapMarker> markers; //Mappa (id POI) -> (marker su mappa)
    private MapFragment.InfoPanel panel; //Pannello che mostra informazioni sul POI selezionato
    private LocationManager locationManager; //Punto di accesso per i sistemi di localizzazione
    private MapView mapView; //Mappa di HERE SDK
    private SearchEngine searchEngine; //Motore di ricerca di HERE SDK
    private double latitude = 0, longitude = 0; //Latitudine e longitudine attuali
    private SharedViewModel viewModel; //ViewModel per l' interazione con il DB
    private DateFormat dateFormat; //Formato per la data
    private Executor executor; //Executor per eseguire operazioni bloccanti in background
    private ActivityResultLauncher<String> requestPermissionLauncher; //Launcher per la richiesta di permessi
    private MapMarker selectedMarker;

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new HashMap<>();
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        executor = Executors.newSingleThreadExecutor();

        if(savedInstanceState != null) {
            selectedMarker = markers.get(savedInstanceState.getString("selectedMarkerId"));
            showPanel(selectedMarker);
        }

        //Listener per ricevere dati da un altro fragment
        getParentFragmentManager().setFragmentResultListener("coordinates", this, (key, bundle) -> {
            double latitude = bundle.getDouble("latitude");
            double longitude = bundle.getDouble("longitude");
            //Centro la mappa sulle coordinate specificate
            centerMapOnCoordinates(latitude, longitude, FOCUSED_ZOOM);
            //Passo automaticamente al tab della mappa
            MainActivity mainActivity = (MainActivity) getActivity();
            if(mainActivity != null)
                mainActivity.switchTab(MainActivity.MAP_TAB);
        });

        //Imposto il launcher per richiedere i permessi
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if(isGranted) {
                askForUpdates();
            } else {
                Toast.makeText(requireActivity(), "Geolocalizzazione disattivata.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
        resetMapButton.setOnClickListener(v -> centerMapOnCoordinates(this.latitude, this.longitude, DEFAULT_ZOOM));
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
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        MainActivity mainActivity = (MainActivity) context;
        //Osservo il lifecycle dell' activity per sapere quando viene creata
        mainActivity.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreateActivity() {
        FragmentActivity activity = requireActivity();
        //Inizializzo il ViewModel per l' interazione con il DB
        viewModel = new ViewModelProvider(activity).get(SharedViewModel.class);
        //Rimuovo il LifecycleObserver
        activity.getLifecycle().removeObserver(this);
    }

    //Centra la mappa sulle coordinate specificate con il fattore di zoom passato
    private void centerMapOnCoordinates(double lat, double lon, double zoomFactor) {
        mapView.getCamera().lookAt(new GeoCoordinates(lat, lon), zoomFactor);
    }

    //Carica la mappa
    private void loadMapScene() {
        //Carica una scena dall' SDK per visualizzare la mappa con un determinato stile
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, mapError -> {
            if(mapError == null) {
                centerMapOnCoordinates(latitude, longitude, DEFAULT_ZOOM);
            } else {
                Log.d(LOG_TAG, "onLoadScene failed: " + mapError.name());
            }
        });
    }

    //Cerca punti di interesse vicini
    private void scanNearbyPois(@NonNull Location location) {
        List<PlaceCategory> categoryList = new ArrayList<>();
        SearchOptions options = new SearchOptions(LanguageCode.EN_US, MAX_MARKERS);
        //Compongo la query
        categoryList.add(new PlaceCategory(PlaceCategory.SIGHTS_AND_MUSEUMS));
        categoryList.add(new PlaceCategory(PlaceCategory.NATURAL_AND_GEOGRAPHICAL));
        CategoryQuery query = new CategoryQuery(categoryList, new GeoCoordinates(latitude, longitude));
        //Effettuo la query
        searchEngine.search(query, options, (searchError, list) -> {
            if(searchError != null) {
                Toast.makeText(requireActivity(), "Errore nella ricerca: " + searchError.toString(), Toast.LENGTH_LONG).show();
            } else if(list != null) {
                //Scorro la lista di luoghi vicini
                for(int i = 0; i < list.size(); i++) {
                    Place place = list.get(i);
                    //Procedo solo se il luogo è valido e ha delle coordinate
                    if(isValid(place) && place.getGeoCoordinates() != null) {
                        //Compongo l' indirizzo
                        String address = place.getAddress().streetName.trim() + " " + place.getAddress().houseNumOrName.trim();
                        String completeAddress = Stream.of(address.trim(), place.getAddress().city.trim(), place.getAddress().country.trim())
                                .filter(str -> str != null && !str.isEmpty())
                                .collect(Collectors.joining(", "));

                        PointOfInterest poi = new PointOfInterest(place.getId(),
                                place.getTitle(),
                                completeAddress,
                                getFormattedDate(location),
                                place.getGeoCoordinates().latitude,
                                place.getGeoCoordinates().longitude,
                                null);

                        boolean visited = isVisited(poi);
                        boolean close = isClose(place);

                        if(close && !visited) {
                            visited = true;
                            viewModel.insertPoi(poi);
                        }

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

    //Restituisce true se il luogo è != null e rientra nelle categorie specificate, false altrimenti
    private boolean isValid(Place place) {
        if(place == null)
            return false;

        String mainCategoryId = place.getDetails().getPrimaryCategories().get(0).getId().split("-")[0];
        return mainCategoryId.equals(PlaceCategory.SIGHTS_AND_MUSEUMS) || mainCategoryId.equals(PlaceCategory.NATURAL_AND_GEOGRAPHICAL);
    }

    //Restituisce true se il POI è già stato visitato in precedenza, false altrimenti
    private boolean isVisited(PointOfInterest poi) {
        if(poi == null)
            return false;

        boolean visited = false;
        if(viewModel.getAllPois().getValue() != null) {
            ArrayList<PointOfInterest> pois = (ArrayList<PointOfInterest>) viewModel.getAllPois().getValue();
            int index;

            if((index = pois.indexOf(poi)) != -1) {
                //Il poi è già presente nel database, pertanto recupero l' immagine da mostrare nella thumbnail dell' infopanel, se presente
                if(pois.get(index).getImagePath() != null)
                    poi.setImagePath(pois.get(index).getImagePath());
                visited = true;
            }
        }

        return visited;
    }

    //Controlla se il luogo in questione è abbastanza vicino da essere considerato "visitato"
    private boolean isClose(Place place) {
        if(place == null)
            return false;

        Integer distance = place.getDistanceInMeters();
        if(distance == null)
            return false;
        else
            return distance < DISTANCE_FOR_POINTS;
    }

    //Cerco il segnalino più distante e lo rimuovo
    private void removeFurthestMarker() {
        GeoCoordinates currentCoordinates = new GeoCoordinates(latitude, longitude);
        MapMarker maxDistanceMarker = null;
        double maxDistance = 0;
        //Cerco il segnalino più distante
        for(MapMarker marker : markers.values()) {
            double distance = marker.getCoordinates().distanceTo(currentCoordinates);
            if(distance > maxDistance) {
                maxDistanceMarker = marker;
                maxDistance = distance;
            }
        }

        if(maxDistanceMarker != null && maxDistanceMarker.getMetadata() != null) {
            markers.remove(maxDistanceMarker.getMetadata().getString("id"));
            mapView.getMapScene().removeMapMarker(maxDistanceMarker);
        }
    }

    //Aggiunge un marker sulla mappa alle coordinate specificate
    private void addMapMarker(@NonNull PointOfInterest poi, boolean visited) {
        GeoCoordinates coordinates = new GeoCoordinates(poi.getLatitude(), poi.getLongitude());
        Metadata metadata = new Metadata();
        Anchor2D anchor = new Anchor2D(0.5f, 1);
        MapImage mapImage;

        //Se ho raggiunto il massimo numero di marker rimuovo il più distante
        if(markers.size() == MAX_MARKERS)
            removeFurthestMarker();

        //Decido il colore del segnalino
        if(visited)
            mapImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_poi_visited);
        else
            mapImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_poi);

        //Creo il segnalino e aggiungo i metadati
        MapMarker marker = new MapMarker(coordinates, mapImage, anchor);
        metadata.setString("id", poi.getId());
        metadata.setString("name", poi.getName());
        metadata.setString("address", poi.getAddress());
        if(poi.getImagePath() != null)
            metadata.setString("imagePath", poi.getImagePath());
        metadata.setString("visited", String.valueOf(visited));
        marker.setMetadata(metadata);

        //Aggiungo il segnalino alla mappa
        mapView.getMapScene().addMapMarker(marker);
        //Aggiungo il seganlino alla lista di MapMarkers
        markers.put(poi.getId(), marker);
    }

    //Seleziona il marker più in alto tra quelli vicini a touchPoint e mostra il pannello con le informazioni sul relativo POI
    private void pickMapMarker(@NonNull Point2D touchPoint) {
        float radiusInPixel = 2;
        mapView.pickMapItems(touchPoint, radiusInPixel, pickMapItemsResult -> {
            if(pickMapItemsResult != null) {
                List<MapMarker> nearbyMarkers = pickMapItemsResult.getMarkers();
                if(nearbyMarkers.size() > 0) {
                    MapMarker topmostMapMarker = nearbyMarkers.get(0);
                    showPanel(topmostMapMarker);

                    restoreMarker();
                    //Sostituisco il marker selezionato con uno identico ma di colore verde
                    selectedMarker = new MapMarker(topmostMapMarker.getCoordinates(),
                            MapImageFactory.fromResource(getResources(), R.drawable.ic_poi_selected),
                            new Anchor2D(0.5f, 1));
                    selectedMarker.setMetadata(topmostMapMarker.getMetadata());
                    mapView.getMapScene().removeMapMarker(topmostMapMarker);
                    mapView.getMapScene().addMapMarker(selectedMarker);
                    if(selectedMarker.getMetadata() != null)
                        markers.replace(selectedMarker.getMetadata().getString("id"), selectedMarker);

                    panel.setVisibility(View.VISIBLE);
                } else {
                    restoreMarker();
                    panel.setVisibility(View.GONE);
                }
            }
        });
    }

    //Mostra l' infopanel popolato dai metadati del POI
    private void showPanel(MapMarker marker) {
        if(marker == null)
            return;

        Metadata metadata = marker.getMetadata();
        if(metadata != null) {
            panel.setLocName(metadata.getString("name"));
            panel.setAddress(metadata.getString("address"));
            panel.setThumbnail(metadata.getString("imagePath"));
        } else {
            //Placeholder in caso di dati mancanti
            panel.setLocName("N/A");
            panel.setAddress("N/A");
            panel.thumbnail.setImageResource(R.drawable.ic_small_placeholder);
        }
    }

    //Riporto il marker deselezionato al suo colore originale
    private void restoreMarker() {
        if(selectedMarker == null)
            return;

        int icon;
        Metadata metadata = selectedMarker.getMetadata();
        //Scelgo l' icona
        if(metadata != null && Boolean.parseBoolean(metadata.getString("visited")))
            icon = R.drawable.ic_poi_visited;
        else
            icon = R.drawable.ic_poi;
        //Creo il marker
        MapMarker marker = new MapMarker(selectedMarker.getCoordinates(),
                MapImageFactory.fromResource(getResources(), icon),
                new Anchor2D(0.5f, 1));
        marker.setMetadata(selectedMarker.getMetadata());
        //Sostituisce il marker selezionato con quello originale
        if(selectedMarker != null)
            mapView.getMapScene().removeMapMarker(selectedMarker);
        mapView.getMapScene().addMapMarker(marker);
        if(selectedMarker.getMetadata() != null)
            markers.replace(selectedMarker.getMetadata().getString("id"), marker);
    }

    //Restituisce, appositamente formattata, la data in cui è stato effettuato il rilevamento di location
    private String getFormattedDate(@NonNull Location location) {
        Date date = new Date(location.getTime());
        return dateFormat.format(date);
    }

    //Restituisce true se uno dei servizi di localizzazione è attivo, false altrimenti
    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    //Chiamato quando la posizione cambia
    public void onLocationChanged(@NonNull Location location) {
        updateLocation(location);
    }

    //Aggiorna la posizione
    private void updateLocation(Location location) {
        if(location == null) {
            return;
        }

        //Recupero le nuove coordinate
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        //Cerco e analizzo i punti di interesse vicini sul thread dell' executor
        executor.execute(() -> scanNearbyPois(location));
        //Aggiorno il marker di posizione
        updateLocationMarker();
    }

    //Chiede all' utente di attivare la localizzazione
    private void showAlert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle("Geolocalizzazione richiesta")
                .setMessage("Abilita il GPS per permettere all' app di rilevare la tua posizione.")
                .setPositiveButton("GPS", (dialogInterface, which) -> {
                    Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gpsIntent);
                })
                .setNegativeButton("Annulla", (dialogInterface, which) ->
                        Toast.makeText(requireContext(), "GPS necessario per rilevare la posizione.", Toast.LENGTH_LONG).show())
                .show();
    }

    //Aggiorna il segnalino che marca la posizione corrente
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
        askForUpdates();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(requireContext(), "GPS disattivato!", Toast.LENGTH_SHORT).show();
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
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        if(selectedMarker != null && selectedMarker.getMetadata() != null)
            bundle.putString("selectedMarkerId", selectedMarker.getMetadata().getString("id"));
        super.onSaveInstanceState(bundle);
    }

    //Mantiene riferimenti ai componenti del pannello informativo
    public static class InfoPanel {
        private CardView card;
        private ImageView thumbnail;
        private TextView locName;
        private TextView address;

        public InfoPanel(View r) {
            if(r != null) {
                card = r.findViewById(R.id.info_panel);
                thumbnail = r.findViewById(R.id.panel_thumbnail);
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

        public void setThumbnail(String path) {
            if(path != null) {
                //Listener per sapere quando l' ImageView è stata creata
                thumbnail.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        thumbnail.removeOnLayoutChangeListener(this);
                        new BitmapHandlingTask(thumbnail.getWidth(), thumbnail.getHeight(), thumbnail).execute(path);
                    }
                });
            } else
                thumbnail.setImageResource(R.drawable.ic_small_placeholder);
        }

        public void setVisibility(int type) {
            if(type != View.VISIBLE && type != View.GONE)
                return;

            card.setVisibility(type);
        }
    }
}