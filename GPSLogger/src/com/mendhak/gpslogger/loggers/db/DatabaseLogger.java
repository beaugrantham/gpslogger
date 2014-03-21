package com.mendhak.gpslogger.loggers.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import com.mendhak.gpslogger.common.RejectionHandler;
import com.mendhak.gpslogger.common.Session;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.db.LocationDbColumnType;
import com.mendhak.gpslogger.db.LocationDbHelper;
import com.mendhak.gpslogger.loggers.ILocationLogger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: beau
 * Date: 3/2/14
 * Time: 9:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseLogger implements ILocationLogger {

   private final int satellites;
   private final Context context;

   private final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(128), new RejectionHandler());

   public DatabaseLogger(Context context, int satellites) {
      this.context = context;
      this.satellites = satellites;
   }

   @Override
   public void log(Location loc) throws Exception {
      if (!Session.hasDescription()) {
         this.log(loc, null);
      }
   }

   @Override
   public void log(Location loc, String annotation) throws Exception {
      DatabaseLogHandler handler = new DatabaseLogHandler(context, loc, annotation, satellites);
      Utilities.LogDebug(String.format("There are currently %s tasks waiting on the DatabaseLogger EXECUTOR.", EXECUTOR.getQueue().size()));
      EXECUTOR.execute(handler);
   }

   @Override
   public String getName() {
      return "db";
   }

}

class DatabaseLogHandler implements Runnable {

   private Location loc;
   private String annotation;
   private int satellites;

   private final LocationDbHelper locationDbHelper;

   public DatabaseLogHandler(Context context, Location loc, String annotation, int satellites) {
      this.loc = loc;
      this.annotation = annotation;
      this.satellites = satellites;

      locationDbHelper = new LocationDbHelper(context);
   }

   @Override
   public void run() {
      SQLiteDatabase db = locationDbHelper.getWritableDatabase();

      ContentValues values = new ContentValues();
      values.put(LocationDbColumnType.LATITUDE.getColumnName(), loc.getLatitude());
      values.put(LocationDbColumnType.LONGITUDE.getColumnName(), loc.getLongitude());
      values.put(LocationDbColumnType.ALTITUDE.getColumnName(), loc.getAltitude());
      values.put(LocationDbColumnType.SPEED.getColumnName(), loc.getSpeed());
      values.put(LocationDbColumnType.ACCURACY.getColumnName(), loc.getAccuracy());
      values.put(LocationDbColumnType.DIRECTION.getColumnName(), loc.getBearing());
      values.put(LocationDbColumnType.TIME.getColumnName(), loc.getTime());
      values.put(LocationDbColumnType.SATELLITES.getColumnName(), this.satellites);
      values.put(LocationDbColumnType.PUBLISHED.getColumnName(), 0);

      if (annotation != null && annotation.length() > 0) {
         values.put(LocationDbColumnType.ANNOTATION.getColumnName(), annotation);
      }

      db.insert(LocationDbHelper.TABLE_NAME, null, values);

      db.close();
   }
}