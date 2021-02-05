package com.narga.landmarkhunter.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.narga.landmarkhunter.data.PointOfInterest;

import java.lang.ref.WeakReference;

//Classe per interagire asincronamente con il database
public class PlacesRepository {
    private static final int PAGE_SIZE = 50;
    private final PointOfInterestDao dao; //Data Access Object per l' interazione con il DB
    private final LiveData<PagedList<PointOfInterest>> poiList;

    public PlacesRepository(Application app) {
        LandmarkHunterDatabase db;
        db = LandmarkHunterDatabase.getLandmarkHunterDatabase(app);
        dao = db.pointOfInterestDao();
        poiList = new LivePagedListBuilder<>(dao.poisByDate(), PAGE_SIZE).build(); //TODO andrebbe creata in background...
    }

    //Inserisce asincronamente un POI nel db
    public void insertPoi(PointOfInterest poi) {
        InsertAsyncTask task = new InsertAsyncTask(dao);
        task.execute(poi);
    }

    //Aggiorna asincronamente il path dell' immagine di un POI
    public void updatePoiImage(String path, String id) {
        UpdateImageAsyncTask task = new UpdateImageAsyncTask(dao);
        task.execute(path, id);
    }

    public void getById(String id, DBQueryListener<PointOfInterest> listener) {
        LookupAsyncTask task = new LookupAsyncTask(dao, listener);
        task.execute(id);
    }

    public void getCount(DBQueryListener<Integer> listener) {
        CountAsyncTask task = new CountAsyncTask(dao, listener);
        task.execute();
    }

    //Restituisce una PagedList di poi, ordinati per data di prima visita, incapsulata in un oggetto LiveData
    public LiveData<PagedList<PointOfInterest>> getPoisByDate() {
        return poiList;
    }

    //Classe per inserire dati nel database in maniera asincrona
    private static class InsertAsyncTask extends AsyncTask<PointOfInterest, Void, Void> {
        private final PointOfInterestDao dao;

        public InsertAsyncTask(PointOfInterestDao d) {
            dao = d;
        }

        @Override
        protected Void doInBackground(PointOfInterest... pois) {
            dao.insertAll(pois);
            return null;
        }
    }

    //Classe per aggiornare entry del database in maniera asincrona
    private static class UpdateImageAsyncTask extends AsyncTask<String, Void, Void> {
        private final PointOfInterestDao dao;

        public UpdateImageAsyncTask(PointOfInterestDao d) {
            dao = d;
        }

        @Override
        protected Void doInBackground(String... params) {
            if(params != null && params.length > 1)
                dao.updateImageById(params[0], params[1]);
            return null;
        }
    }

    //Classe per effettuare lookup in maniera asincrona
    private static class LookupAsyncTask extends AsyncTask<String, Void, PointOfInterest> {
        private final PointOfInterestDao dao;
        private DBQueryListener<PointOfInterest> listener;

        public LookupAsyncTask(PointOfInterestDao d, DBQueryListener<PointOfInterest> listener) {
            dao = d;
            this.listener = listener;
        }

        @Override
        protected PointOfInterest doInBackground(String... params) {
            if(params != null && params.length > 1) {
                return dao.getById(params[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(PointOfInterest pointOfInterest) {
            if(listener != null)
                listener.onQueryResult(pointOfInterest);
        }
    }

    private static class CountAsyncTask extends AsyncTask<Void, Void, Integer> {
        private final PointOfInterestDao dao;
        private DBQueryListener<Integer> listener;

        public CountAsyncTask(PointOfInterestDao d, DBQueryListener<Integer> listener) {
            dao = d;
            this.listener = listener;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return dao.countVisited();
        }

        @Override
        protected void onPostExecute(Integer count) {
            if(listener != null)
                listener.onQueryResult(count);
        }
    }

    public interface DBQueryListener<T> {
        void onQueryResult(T result);
    }
}
