package com.narga.landmarkhunter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
        holder.thumbnail.setImageResource(R.drawable.ic_image_placeholder);
        holder.name.setText(items.get(position).getName());
        holder.address.setText(items.get(position).getAddress());
        holder.date.setText(items.get(position).getDate());
        holder.uris = items.get(position).getImagePaths();
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
        public final ImageButton openImageGallery;
        public ArrayList<String> uris;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.entry_card);
            thumbnail = itemView.findViewById(R.id.entry_thumbnail);
            name = itemView.findViewById(R.id.entry_name);
            address = itemView.findViewById(R.id.entry_address);
            date = itemView.findViewById(R.id.entry_date_visited);
            openImageGallery = itemView.findViewById(R.id.entry_image_gallery);

            openImageGallery.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), GalleryActivity.class);
            intent.putExtra("URIs", uris);
            v.getContext().startActivity(intent);
        }
    }
}
