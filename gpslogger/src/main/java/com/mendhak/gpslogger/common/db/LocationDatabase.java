package com.mendhak.gpslogger.common.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Point.class}, version = 1, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {

    private static LocationDatabase instance;

    public abstract PointDao pointDao();

    public static LocationDatabase getLocationDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), LocationDatabase.class, "gpslogger-db").build();
        }

        return instance;
    }

}