package com.mendhak.gpslogger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import com.mendhak.gpslogger.loggers.ILocationLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/24/14
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocationDbHelper extends SQLiteOpenHelper implements ILocationLogger {
   public static final int DATABASE_VERSION = 1;

   public static final String DATABASE_NAME = "gpslogger.db";

   private static final String TABLE_NAME = "location";

   public static final String COLUMN_NAME_ID = "id";
   public static final String COLUMN_NAME_LATITUDE = "latitude";
   public static final String COLUMN_NAME_LONGITUDE = "longitude";
   public static final String COLUMN_NAME_ANNOTATION = "annotation";
   public static final String COLUMN_NAME_SATELLITES = "satellites";
   public static final String COLUMN_NAME_ALTITUDE = "altitude";
   public static final String COLUMN_NAME_SPEED = "speed";
   public static final String COLUMN_NAME_ACCURACY = "accuracy";
   public static final String COLUMN_NAME_DIRECTION = "direction";
   public static final String COLUMN_NAME_PROVIDER = "provider";
   public static final String COLUMN_NAME_TIME = "time";

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
   public void log(Location loc) throws Exception {
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
   public void log(String description, Location loc) throws Exception {
      // TODO
   }

   @Override
   public String getName() {
      return "db";
   }

   public List<String> getRecords() {
      List<String> records = new ArrayList<String>();

      SQLiteDatabase db = this.getWritableDatabase();
      Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_NAME_ID, COLUMN_NAME_LATITUDE, COLUMN_NAME_LONGITUDE}, null, null, null, null, null);

      if (cursor.moveToFirst()) {
         do {
            String s = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID));
            s += ":" + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LATITUDE));
            s += ":" + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LONGITUDE));

            records.add(s);
         } while (cursor.moveToNext());
      }

      return records;
   }

   public List<Map<String, String>> getRecordsAsMap() {
      List<Map<String, String>> records = new ArrayList<Map<String, String>>();

      SQLiteDatabase db = this.getReadableDatabase();
      Cursor cursor = db.query(
              TABLE_NAME,
              new String[] {
                      COLUMN_NAME_ID,
                      COLUMN_NAME_LATITUDE,
                      COLUMN_NAME_LONGITUDE,
                      COLUMN_NAME_ANNOTATION,
                      COLUMN_NAME_SATELLITES,
                      COLUMN_NAME_ALTITUDE,
                      COLUMN_NAME_SPEED,
                      COLUMN_NAME_ACCURACY,
                      COLUMN_NAME_DIRECTION,
                      COLUMN_NAME_PROVIDER,
                      COLUMN_NAME_TIME
              }, null, null, null, null, null);

      if (cursor.moveToFirst()) {
         do {
            Map<String, String> map = new HashMap<String, String>();

            map.put(COLUMN_NAME_ID, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ID)));
            map.put(COLUMN_NAME_LATITUDE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LATITUDE)));
            map.put(COLUMN_NAME_LONGITUDE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LONGITUDE)));
            map.put(COLUMN_NAME_ANNOTATION, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ANNOTATION)));
            map.put(COLUMN_NAME_SATELLITES, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SATELLITES)));
            map.put(COLUMN_NAME_ALTITUDE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ALTITUDE)));
            map.put(COLUMN_NAME_SPEED, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SPEED)));
            map.put(COLUMN_NAME_ACCURACY, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ACCURACY)));
            map.put(COLUMN_NAME_DIRECTION, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DIRECTION)));
            map.put(COLUMN_NAME_PROVIDER, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_PROVIDER)));
            map.put(COLUMN_NAME_TIME, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_TIME)));

            records.add(map);
         } while (cursor.moveToNext());
      }

      return records;
   }
}
