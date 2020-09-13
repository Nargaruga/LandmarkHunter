package com.narga.landmarkhunter;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.List;

//Mantiene un' istanza del repository e offre metodi per accedervi
public class SharedViewModel extends AndroidViewModel {
    private final PlacesRepository repository;
    private final MutableLiveData<List<PointOfInterest>> searchResults;
    private final LiveData<List<PointOfInterest>> allPois;

    public SharedViewModel(@NonNull Application app) {
        super(app);
        //Crea un repository passandovi il contesto dell' applicazione
        repository = new PlacesRepository(app);
        //Ottiene i riferimenti ai dati da osservare
        allPois = repository.getAllPois();
        searchResults = repository.getSearchResults();
    }

    MutableLiveData<List<PointOfInterest>> getSearchResults() {
        return searchResults;
    }

    LiveData<List<PointOfInterest>> getAllPois() {
        return allPois;
    }

    public void insertPoi(PointOfInterest poi) {
        repository.insertPoi(poi);
    }

    public void clearPois() {
        repository.clearPoiTable();
    }

    public int getPoiCount() {
        return repository.getPoiCount();
    }

}
