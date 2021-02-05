package com.narga.landmarkhunter.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagedList;

import com.narga.landmarkhunter.data.PointOfInterest;

import java.util.List;

//Mantiene un' istanza del repository e offre metodi per accedervi
public class SharedViewModel extends AndroidViewModel {
    private final PlacesRepository repository; //Repository per l' interazione asincrona con il DB
    private final LiveData<PagedList<PointOfInterest>> poiList; //Dati osservati

    public SharedViewModel(@NonNull Application app) {
        super(app);
        //Crea un repository passandovi il contesto dell' applicazione
        repository = new PlacesRepository(app);
        //Ottiene i riferimenti ai dati da osservare
        poiList = repository.getPoisByDate();

    }

    public LiveData<PagedList<PointOfInterest>> getPoisByDate() {
        return poiList;
    }

    public void getById(String id, PlacesRepository.DBQueryListener<PointOfInterest> listener) {
        repository.getById(id, listener);
    }

    public void getCount(PlacesRepository.DBQueryListener<Integer> listener) {
        repository.getCount(listener);
    }

    public void insertPoi(PointOfInterest poi) {
        repository.insertPoi(poi);
    }

    public void updatePoiImage(String path, String id) {
        repository.updatePoiImage(path, id);
        PagedList<PointOfInterest> pois = poiList.getValue();
        if(pois != null)
            pois.getDataSource().invalidate();
    }
}
