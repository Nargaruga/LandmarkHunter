package com.narga.landmarkhunter.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.narga.landmarkhunter.data.PointOfInterest;

import java.util.List;

//Mantiene un' istanza del repository e offre metodi per accedervi
public class SharedViewModel extends AndroidViewModel {
    private final PlacesRepository repository; //Repository per l' interazione asincrona con il DB
    private final LiveData<List<PointOfInterest>> allPois; //Dati osservati

    public SharedViewModel(@NonNull Application app) {
        super(app);
        //Crea un repository passandovi il contesto dell' applicazione
        repository = new PlacesRepository(app);
        //Ottiene i riferimenti ai dati da osservare
        allPois = repository.getAllPois();
    }

    public LiveData<List<PointOfInterest>> getAllPois() {
        return allPois;
    }

    public void insertPoi(PointOfInterest poi) {
        repository.insertPoi(poi);
    }

    public void updatePoiImage(String path, String id) {
        repository.updatePoiImage(path, id);
    }

}
