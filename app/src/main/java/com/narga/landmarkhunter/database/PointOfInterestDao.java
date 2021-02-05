package com.narga.landmarkhunter.database;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.narga.landmarkhunter.data.PointOfInterest;

@Dao
public interface PointOfInterestDao {
    @Query("SELECT * FROM point_of_interest ORDER BY date DESC")
    DataSource.Factory<Integer, PointOfInterest> poisByDate();

    @Query("SELECT COUNT(_id) FROM point_of_interest")
    int countVisited();

    @Insert()
    void insertAll(PointOfInterest... pois);

    @Query("UPDATE point_of_interest SET image_path=:path WHERE _id=:id")
    void updateImageById(String path, String id);

    @Query("SELECT _id FROM point_of_interest WHERE _id=:id")
    PointOfInterest getById(String id);
}
