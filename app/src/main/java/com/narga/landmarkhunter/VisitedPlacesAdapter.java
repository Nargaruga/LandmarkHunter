package com.narga.landmarkhunter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

//Adapter che associa gli elementi di un dataset alle view di una RecyclerView
public class VisitedPlacesAdapter extends RecyclerView.Adapter<VisitedPlacesAdapter.SearchViewHolder> implements Filterable {
    private static final String LOG_TAG = VisitedPlacesAdapter.class.getSimpleName();
    private ArrayList<PointOfInterest> items; //Contiene i POI corrispondenti ai criteri di ricerca correnti
    private ArrayList<PointOfInterest> allItems; //Contiene tutti i POI
    private Filter filter; //Filtro per la ricerca

    public VisitedPlacesAdapter(ArrayList<PointOfInterest> items) {
        this.items = items;
        allItems = new ArrayList<>(items);

        filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<PointOfInterest> filteredList = new ArrayList<>();
                if(constraint.toString().isEmpty()) {
                    filteredList.addAll(allItems);
                } else {
                    for(PointOfInterest poi : allItems) {
                        if(poi.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredList.add(poi);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;

                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items.clear();
                items.addAll((List<PointOfInterest>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public VisitedPlacesAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VisitedPlacesAdapter.SearchViewHolder holder, int position) {
        //Rimuovo la thumbnail per evitare che ne venga riutilizzata una sbagliata durante il riciclo del ViewHolder
        holder.thumbnail.setImageBitmap(null);
        //Imposto nome del luogo, indirizzo e data in cui è stato visitato
        holder.name.setText(items.get(position).getName());
        holder.address.setText(items.get(position).getAddress());
        holder.date.setText(items.get(position).getDate());
        //Imposto il percorso dell' immagine e l' id del POI
        holder.path = items.get(position).getImagePath();
        holder.id = items.get(position).getId();
        //Imposto latitudine e longitudine
        holder.latitude = items.get(position).getLatitude();
        holder.longitude = items.get(position).getLongitude();
        //Listener per attendere che l' ImageView venga inizializzata
        holder.thumbnail.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                holder.thumbnail.removeOnLayoutChangeListener(this);
                //Imposto la thumbnail con una bitmap scalata appositamente o un placeholder nel caso non sia stata impostata un' immagine
                if(holder.path != null)
                    new BitmapHandlingTask(holder.thumbnail.getWidth(), holder.thumbnail.getHeight(), holder.thumbnail).execute(holder.path);
                else
                    holder.thumbnail.setImageResource(R.drawable.ic_placeholder);
            }
        });
    }

    @Override
    //Restituisce il numero di elementi del dataset
    public int getItemCount() {
        if(items != null)
            return items.size();
        else
            return 0;
    }

    //Imposta la lista di elementi
    public void setItems(List<PointOfInterest> newItems) {
        items.clear();
        allItems.clear();
        items.addAll(newItems);
        allItems.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }


    //Mantiene riferimenti agli elementi di una entry della RecyclerView
    public static class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final CardView cardView;
        public final ImageView thumbnail;
        public final TextView name;
        public final TextView address;
        public final TextView date;
        public final ImageButton locateOnMap;
        public String path;
        public String id;
        public double latitude;
        public double longitude;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.entry_card);
            thumbnail = itemView.findViewById(R.id.entry_thumbnail);
            name = itemView.findViewById(R.id.entry_name);
            address = itemView.findViewById(R.id.entry_address);
            date = itemView.findViewById(R.id.entry_date_visited);
            locateOnMap = itemView.findViewById(R.id.locate_on_map);

            thumbnail.setOnClickListener(this);
            locateOnMap.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == thumbnail.getId()) {
                //Apro l' immagine a schermo intero
                Intent intent = new Intent(v.getContext(), LargeImageActivity.class);
                intent.putExtra("path", path);
                intent.putExtra("id", id);
                v.getContext().startActivity(intent);
            } else if(v.getId() == locateOnMap.getId()) {
                //Passo le coordinate del luogo al map fragment, in modo da potervi centrare la mappa
                Bundle coordinatesBundle = new Bundle();
                coordinatesBundle.putDouble("latitude", latitude);
                coordinatesBundle.putDouble("longitude", longitude);
                FragmentManager.findFragment(v).getParentFragmentManager().setFragmentResult("coordinates", coordinatesBundle);
            }
        }
    }
}
