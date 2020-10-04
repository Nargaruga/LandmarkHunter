package com.narga.landmarkhunter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

//Classe per caricare una versione downscalata di un immagine all' interno di una ImageView
public class BitmapHandlingTask extends AsyncTask<String, Void, Bitmap> {
    private int desiredWidth;
    private int desiredHeight;
    private WeakReference<ImageView> imageView;

    public BitmapHandlingTask(int desiredWidth, int desiredHeight, ImageView imageView) {
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;
        this.imageView = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        if(strings == null || strings[0] == null)
            return null;

        String filepath = strings[0];
        int imageWidth, imageHeight;
        String mimeType; //TODO

        //Recupero informazioni sull' immagine
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;
        mimeType = options.outMimeType;

        options.inSampleSize = calculateIInSampleSize(imageWidth, imageHeight, desiredWidth, desiredHeight);
        options.outMimeType = mimeType;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filepath, options);
    }

    //Calcola il fattore che decide di quanto downscalare l' immagine caricata in memoria
    public int calculateIInSampleSize(int width, int height, int desiredWidth, int desiredHeight) {
        int sampleSize = 1;

        while(width / sampleSize > desiredWidth || height / sampleSize > desiredHeight)
            sampleSize *= 2;

        return sampleSize;
    }

    //Metodo per convertire da dp a px basandosi sulla densità dello schermo
    public int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if(imageView.get() != null)
            imageView.get().setImageBitmap(bitmap);
    }
}
