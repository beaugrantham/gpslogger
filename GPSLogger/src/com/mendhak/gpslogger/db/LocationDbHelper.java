package com.mendhak.gpslogger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.mendhak.gpslogger.common.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 2/24/14
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocationDbHelper extends SQLiteOpenHelper {
   public static final int DATABASE_VERSION = 3;

   public static final String DATABASE_NAME = "gpslogger.db";

   public static final String TABLE_NAME = "location";

   public LocationDbHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   public void onCreate(SQLiteDatabase db) {
      final String createLocationTable =
              "CREATE TABLE " + TABLE_NAME + " (" +
                      LocationDbColumnType.ID.getColumnName() + " INTEGER PRIMARY KEY, " +
                      LocationDbColumnType.LATITUDE.getColumnName() + " TEXT, " +
                      LocationDbColumnType.LONGITUDE.getColumnName() + " TEXT, " +
                      LocationDbColumnType.ANNOTATION.getColumnName() + " TEXT, " +
                      LocationDbColumnType.SATELLITES.getColumnName() + " TEXT, " +
                      LocationDbColumnType.ALTITUDE.getColumnName()  + " TEXT, " +
                      LocationDbColumnType.SPEED.getColumnName() + " TEXT, " +
                      LocationDbColumnType.ACCURACY.getColumnName() + " TEXT, " +
                      LocationDbColumnType.DIRECTION.getColumnName() + " TEXT, " +
                      LocationDbColumnType.PROVIDER.getColumnName() + " TEXT, " +
                      LocationDbColumnType.TIME.getColumnName() + " TEXT, " +
                      LocationDbColumnType.PUBLISHED.getColumnName() + " INTEGER" +
                      " )";

      db.execSQL(createLocationTable);
   }

   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      final String dropLocationTable = "DROP TABLE IF EXISTS " + TABLE_NAME;

      db.execSQL(dropLocationTable);
      this.onCreate(db);
   }

   public List<Map<String, String>> getRecords() {
      List<Map<String, String>> records = new ArrayList<Map<String, String>>();

      SQLiteDatabase db = this.getReadableDatabase();
      Cursor cursor = db.query(TABLE_NAME, LocationDbColumnType.getColumnNames(), null, null, null, null, null);

      if (cursor.moveToFirst()) {
         do {
            Map<String, String> map = new HashMap<String, String>();

            for (String columnName : LocationDbColumnType.getColumnNames()) {
               map.put(columnName, cursor.getString(cursor.getColumnIndexOrThrow(columnName)));
            }

            records.add(map);
         } while (cursor.moveToNext());
      }

      return records;
   }

   public Map<Integer, Map<String, String>> getUnPublishedRecords() {
      Map<Integer, Map<String, String>> records = new TreeMap<Integer, Map<String, String>>();

      SQLiteDatabase db = this.getReadableDatabase();
      Cursor cursor = db.query(
              TABLE_NAME,
              LocationDbColumnType.getColumnNames(),
              LocationDbColumnType.PUBLISHED + " = 0", null,
              null, null,
              LocationDbColumnType.ID.getColumnName());

      if (cursor.moveToFirst()) {
         do {
            Map<String, String> record = new HashMap<String, String>();

            for (String columnName : LocationDbColumnType.getColumnNames()) {
               record.put(columnName, cursor.getString(cursor.getColumnIndexOrThrow(columnName)));
            }

            records.put(Integer.parseInt(record.get(LocationDbColumnType.ID.getColumnName())), record);
         } while (cursor.moveToNext());
      }

      return records;
   }

   public int publish(Collection<Integer> ids) {
      SQLiteDatabase db = this.getWritableDatabase();

      ContentValues cv = new ContentValues();
      cv.put(LocationDbColumnType.PUBLISHED.getColumnName(), 1);

      int rowsUpdated = db.update(TABLE_NAME, cv, LocationDbColumnType.ID.getColumnName() + " IN (" + TextUtils.join(",", ids) + ")", null);

      db.close();

      Utilities.logInfo("Flagged " + rowsUpdated + " as published");

      return rowsUpdated;
   }
}
