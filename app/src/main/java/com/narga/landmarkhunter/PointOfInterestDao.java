package com.narga.landmarkhunter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PointOfInterestDao {
    @Query("SELECT * FROM point_of_interest")
    LiveData<List<PointOfInterest>> getAll();

    @Query("SELECT * FROM point_of_interest where name LIKE :name")
    List<PointOfInterest> findByName(String name);

    @Insert()
    void insertAll(PointOfInterest... pois);

    @Query("UPDATE point_of_interest SET image_path=:path WHERE id=:id")
    void updateImageById(String path, String id);

    @Delete
    void delete(PointOfInterest poi);

    @Query("DELETE FROM point_of_interest")
    void nukeTable();
}
