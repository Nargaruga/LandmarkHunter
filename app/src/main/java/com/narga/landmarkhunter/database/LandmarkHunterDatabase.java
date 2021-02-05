package com.narga.landmarkhunter.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.narga.landmarkhunter.data.PointOfInterest;

@Database(entities = PointOfInterest.class, version = 2)
public abstract class LandmarkHunterDatabase extends RoomDatabase {
    private static LandmarkHunterDatabase INSTANCE; //Riferimento all' unica istanza del DB

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
