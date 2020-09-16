package com.narga.landmarkhunter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

public class BitmapHandler extends AsyncTask<Uri, Void, Bitmap> {
    GalleryAdapter.ImageViewHolder viewHolder;

    public BitmapHandler(GalleryAdapter.ImageViewHolder viewHolder){
        this.viewHolder = viewHolder;
    }

    @Override
    protected Bitmap doInBackground(Uri... uris) {
        if(uris == null || uris[0] == null || viewHolder == null)
            return null;

        String filepath = uris[0].toString();
        int imageWidth, imageHeight;
        String imageType;

        //Recupero informazioni sull' immagine identificata dall' URI
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;
        imageType = options.outMimeType;



        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
    }
}
