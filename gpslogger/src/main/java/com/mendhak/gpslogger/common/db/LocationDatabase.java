package com.mendhak.gpslogger.common.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@Database(entities = {Point.class}, version = 3, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {

    private static LocationDatabase instance;

    public abstract PointDao pointDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE point ADD COLUMN annotation TEXT");
            database.execSQL("ALTER TABLE point ADD COLUMN media TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DELETE FROM point");
        }
    };

    public static LocationDatabase getLocationDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), LocationDatabase.class, "gpslogger-db").addMigrations(MIGRATION_2_3).build();
        }

        return instance;
    }

}