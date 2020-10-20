package com.narga.landmarkhunter.utility;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.lang.ref.WeakReference;

//Classe per effettuare asincronamente query al ContentResolver
public class QueryHandler extends AsyncQueryHandler {
    private WeakReference<AsyncQueryListener> listenerRef;
    public QueryHandler(ContentResolver contentResolver, AsyncQueryListener listener) {
        super(contentResolver);
        listenerRef = new WeakReference<>(listener);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        AsyncQueryListener listener = listenerRef.get();
        if(listener != null) {
            listener.onQueryComplete(token, cookie, cursor);
        } else if(cursor != null) {
            cursor.close();
        }
    }

    //Interfaccia da implementare per essere notificati al termine della query
    public interface AsyncQueryListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
    }
}
