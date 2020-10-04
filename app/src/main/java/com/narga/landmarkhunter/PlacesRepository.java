package com.narga.landmarkhunter;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

//Classe per interagire asincronamente con il database
public class PlacesRepository {
    private final PointOfInterestDao dao;
    private final MutableLiveData<List<PointOfInterest>> searchResults = new MutableLiveData<>(); //Risultati dell' ultima ricerca
    private final LiveData<List<PointOfInterest>> allPois; //Dati osservati dal ViewModel

    public PlacesRepository(Application app) {
        LandmarkHunterDatabase db;
        db = LandmarkHunterDatabase.getLandmarkHunterDatabase(app);
        dao = db.pointOfInterestDao();
        allPois = dao.getAll();
    }

    //Imposta i searchResults
    private void asyncFinished(List<PointOfInterest> results) {
        searchResults.setValue(results);
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

    //Effettua asincronamente una query sul database
    public void findPoi(String name) {
        QueryAsyncTask task = new QueryAsyncTask(dao);
        task.setRepository(this);
        task.execute(name);
    }

    //Svuota asincronamente la tabella
    public void clearPoiTable() {
        NukeTableAsyncTask task = new NukeTableAsyncTask(dao);
        task.execute();
    }

    //Restituisce il numero di POI
    public int getPoiCount() {
        if(allPois != null && allPois.getValue() != null)
            return allPois.getValue().size();
        else
            return 0;
    }

    //Restituisce i risultati della ricerca
    public MutableLiveData<List<PointOfInterest>> getSearchResults() {
        return searchResults;
    }

    //Restituisce tutti i POI
    public LiveData<List<PointOfInterest>> getAllPois() {
        return allPois;
    }


    //Classe per effettuare query in maniera asincrona
    private static class QueryAsyncTask extends AsyncTask<String, Void, List<PointOfInterest>> {
        private final PointOfInterestDao dao;
        private PlacesRepository repository;

        public QueryAsyncTask(PointOfInterestDao d) {
            dao = d;
        }

        @Override
        //Ottiene asincronamente la lista di POI il cui nome è simile a strings[0] e la restituisce
        protected List<PointOfInterest> doInBackground(String... strings) {
            return dao.findByName(strings[0]);
        }

        @Override
        //Chiamato al termine dell' esecuzione
        protected void onPostExecute(List<PointOfInterest> result) {
            repository.asyncFinished(result);
        }

        //Imposta il repository a cui restituire i risultati
        public void setRepository(PlacesRepository r) {
            repository = r;
        }
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


    //Classe per svuotare una tabella in maniera asincrona
    private static class NukeTableAsyncTask extends AsyncTask<Void, Void, Void> {
        private final PointOfInterestDao dao;

        public NukeTableAsyncTask(PointOfInterestDao d) {
            dao = d;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao.nukeTable();
            return null;
        }
    }
}
