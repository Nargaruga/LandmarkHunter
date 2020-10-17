package com.narga.landmarkhunter;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = PointOfInterest.class, version = 1)
public abstract class LandmarkHunterDatabase extends RoomDatabase {
    private static LandmarkHunterDatabase INSTANCE;

    public abstract PointOfInterestDao pointOfInterestDao();

    //NON UTILIZZARE, chiamare anzi getLandmarkHunterDatabase
    public LandmarkHunterDatabase() {
    }

    //Metodo per ottenere l' istanza del DB
    public static LandmarkHunterDatabase getLandmarkHunterDatabase(Context context) {
        if(INSTANCE == null)
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LandmarkHunterDatabase.class, "point_of_interest_database").build();
        return INSTANCE;
    }
}
