package com.narga.landmarkhunter.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.narga.landmarkhunter.data.PointOfInterest;

import java.util.List;

@Dao
public interface PointOfInterestDao {
    @Query("SELECT * FROM point_of_interest")
    LiveData<List<PointOfInterest>> getAll();

    @Insert()
    void insertAll(PointOfInterest... pois);

    @Query("UPDATE point_of_interest SET image_path=:path WHERE id=:id")
    void updateImageById(String path, String id);
}
