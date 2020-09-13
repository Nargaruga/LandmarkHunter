package com.narga.landmarkhunter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

@Database(entities = PointOfInterest.class, version = 1)
public abstract class LandmarkHunterDatabase extends RoomDatabase {
    private static LandmarkHunterDatabase INSTANCE;
    private PointOfInterestDao dao;

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

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }

    public static void clearInstance() {
        INSTANCE = null;
    }
}
