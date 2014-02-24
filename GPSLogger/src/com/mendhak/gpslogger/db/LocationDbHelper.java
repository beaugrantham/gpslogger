package com.mendhak.gpslogger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import com.mendhak.gpslogger.loggers.IFileLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/24/14
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocationDbHelper extends SQLiteOpenHelper implements IFileLogger {
   public static final int DATABASE_VERSION = 1;

   public static final String DATABASE_NAME = "gpslogger.db";

   private static final String TABLE_NAME = "location";
   private static final String COLUMN_NAME_ID = "id";
   private static final String COLUMN_NAME_LATITUDE = "latitude";
   private static final String COLUMN_NAME_LONGITUDE = "longitude";
   private static final String COLUMN_NAME_ANNOTATION = "annotation";
   private static final String COLUMN_NAME_SATELLITES = "satellites";
   private static final String COLUMN_NAME_ALTITUDE = "altitude";
   private static final String COLUMN_NAME_SPEED = "speed";
   private static final String COLUMN_NAME_ACCURACY = "accuracy";
   private static final String COLUMN_NAME_DIRECTION = "direction";
   private static final String COLUMN_NAME_PROVIDER = "provider";
   private static final String COLUMN_NAME_TIME = "time";

   public LocationDbHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   public void onCreate(SQLiteDatabase db) {
      final String createLocationTable =
              "CREATE TABLE " + TABLE_NAME + " (" +
                      COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
                      COLUMN_NAME_LATITUDE + " TEXT, " +
                      COLUMN_NAME_LONGITUDE + " TEXT, " +
                      COLUMN_NAME_ANNOTATION + " TEXT, " +
                      COLUMN_NAME_SATELLITES + " TEXT, " +
                      COLUMN_NAME_ALTITUDE + " TEXT, " +
                      COLUMN_NAME_SPEED + " TEXT, " +
                      COLUMN_NAME_ACCURACY + " TEXT, " +
                      COLUMN_NAME_DIRECTION + " TEXT, " +
                      COLUMN_NAME_PROVIDER + " TEXT, " +
                      COLUMN_NAME_TIME + " TEXT" +
                      " )";

      db.execSQL(createLocationTable);
   }

   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      final String dropLocationTable = "DROP TABLE IF EXISTS " + TABLE_NAME;

      db.execSQL(dropLocationTable);
      this.onCreate(db);
   }

   @Override
   public void Write(Location loc) throws Exception {
      SQLiteDatabase db = this.getWritableDatabase();

      ContentValues values = new ContentValues();
      values.put(COLUMN_NAME_LATITUDE, loc.getLatitude());
      values.put(COLUMN_NAME_LONGITUDE, loc.getLongitude());
      values.put(COLUMN_NAME_ALTITUDE, loc.getAltitude());
      values.put(COLUMN_NAME_SPEED, loc.getSpeed());
      values.put(COLUMN_NAME_ACCURACY, loc.getAccuracy());
      values.put(COLUMN_NAME_DIRECTION, loc.getBearing());
      values.put(COLUMN_NAME_TIME, loc.getTime());

      db.insert(TABLE_NAME, null, values);

      db.close();
   }

   @Override
   public void Annotate(String description, Location loc) throws Exception {
      // TODO
   }

   @Override
   public String getName() {
      return "db";
   }
}
