package com.narga.landmarkhunter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

//Adapter che associa gli elementi di un dataset alle view di una RecyclerView
public class VisitedPlacesAdapter extends RecyclerView.Adapter<VisitedPlacesAdapter.SearchViewHolder> {
    private static final String LOG_TAG = VisitedPlacesAdapter.class.getSimpleName();
    private final ArrayList<PointOfInterest> items;

    public VisitedPlacesAdapter(ArrayList<PointOfInterest> i) {
        items = i;
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
        //Imposto la thumbnail con una bitmap scalata appositamente o un placeholder nel caso non sia stata impostata un' immagine
        holder.thumbnail.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                holder.thumbnail.removeOnLayoutChangeListener(this);
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
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    //Mantiene riferimenti agli elementi di una entry della RecyclerView
    public static class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final CardView cardView;
        public final ImageView thumbnail;
        public final TextView name;
        public final TextView address;
        public final TextView date;
        public String path;
        public String id;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.entry_card);
            thumbnail = itemView.findViewById(R.id.entry_thumbnail);
            name = itemView.findViewById(R.id.entry_name);
            address = itemView.findViewById(R.id.entry_address);
            date = itemView.findViewById(R.id.entry_date_visited);

            thumbnail.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), LargeImageActivity.class);
            intent.putExtra("path", path);
            intent.putExtra("id", id);
            v.getContext().startActivity(intent);
        }
    }
}
