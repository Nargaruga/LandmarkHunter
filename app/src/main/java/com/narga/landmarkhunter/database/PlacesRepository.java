package com.narga.landmarkhunter.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.narga.landmarkhunter.data.PointOfInterest;

import java.util.List;

//Classe per interagire asincronamente con il database
public class PlacesRepository {
    private final PointOfInterestDao dao; //Data Access Object per l' interazione con il DB
    private final LiveData<List<PointOfInterest>> allPois; //Dati osservati dal ViewModel

    public PlacesRepository(Application app) {
        LandmarkHunterDatabase db;
        db = LandmarkHunterDatabase.getLandmarkHunterDatabase(app);
        dao = db.pointOfInterestDao();
        allPois = dao.getAll();
    }

    //Inserisce asincronamente un POI nel db
    public void insertPoi(PointOfInterest poi) {
        InsertAsyncTask task = new InsertAsyncTask(dao);
        task.execute(poi);
    }

    //Aggiorna asincronamente il path dell' immagine di un POI
    public void updatePoiImage(String path, String id){
        UpdateImageAsyncTask task = new UpdateImageAsyncTask(dao);
        task.execute(path, id);
    }

    //Restituisce tutti i POI
    public LiveData<List<PointOfInterest>> getAllPois() {
        return allPois;
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
}
