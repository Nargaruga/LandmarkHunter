package com.narga.landmarkhunter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {
    private static final String LOG_TAG = GalleryAdapter.class.getSimpleName();
    private ArrayList<Uri> items;

    public GalleryAdapter(ArrayList<Uri> i) {
        items = i;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = items.get(position);
        holder.image.setImageURI(uri);
        //TODO
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
    public void addItem(Uri uri) {
        items.add(uri);
        notifyDataSetChanged(); //TODO EFFICIENTE?
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.gallery_image);
        }
    }
}
