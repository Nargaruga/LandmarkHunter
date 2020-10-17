package com.narga.landmarkhunter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;

import java.lang.ref.WeakReference;

//Classe per caricare una versione downscalata di un immagine all' interno di una ImageView
public class BitmapHandlingTask extends AsyncTask<String, Void, Bitmap> {
    private static final String LOG_TAG = BitmapHandlingTask.class.getSimpleName();
    static final int IMAGE_VIEW = 0;
    static final int PHOTO_VIEW = 1;
    private int mode;
    private int desiredWidth;
    private int desiredHeight;
    private WeakReference<ImageView> imageViewRef;
    private WeakReference<PhotoView> photoViewRef;

    public BitmapHandlingTask(int desiredWidth, int desiredHeight, ImageView imageView) {
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;
        this.imageViewRef = new WeakReference<>(imageView);
        mode = IMAGE_VIEW;
    }

    public BitmapHandlingTask(int desiredWidth, int desiredHeight, PhotoView photoView) {
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;
        this.photoViewRef = new WeakReference<>(photoView);
        mode = PHOTO_VIEW;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        if(strings == null || strings[0] == null)
            return null;

        String filepath = strings[0];
        int imageWidth, imageHeight;
        String mimeType;

        //Recupero informazioni sull' immagine
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;
        mimeType = options.outMimeType;

        options.inSampleSize = calculateInSampleSize(imageWidth, imageHeight, desiredWidth, desiredHeight);
        options.outMimeType = mimeType;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filepath, options);
    }

    //Calcola il fattore che decide di quanto downscalare l' immagine caricata in memoria
    public int calculateInSampleSize(int width, int height, int desiredWidth, int desiredHeight) {
        int sampleSize = 1;

        while(width / sampleSize > desiredWidth || height / sampleSize > desiredHeight)
            sampleSize *= 2;

        return sampleSize;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        switch(mode) {
            case IMAGE_VIEW:
                if(imageViewRef.get() != null) {
                    if(bitmap == null)
                        imageViewRef.get().setImageResource(R.drawable.ic_placeholder);
                    else
                        imageViewRef.get().setImageBitmap(bitmap);
                }
                break;
            case PHOTO_VIEW:
                if(photoViewRef.get() != null) {
                    if(bitmap == null) {
                        photoViewRef.get().setImageResource(R.drawable.ic_placeholder);
                        photoViewRef.get().setZoomable(false); //Impedisco all' utente di zoommare il placeholder
                    } else {
                        photoViewRef.get().setImageBitmap(bitmap);
                        photoViewRef.get().setZoomable(true);
                    }
                }
        }
    }
}
