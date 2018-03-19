package com.mendhak.gpslogger.loggers.db;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.mendhak.gpslogger.common.SerializableLocation;
import com.mendhak.gpslogger.common.db.LocationDatabase;
import com.mendhak.gpslogger.common.db.Point;
import com.mendhak.gpslogger.common.slf4j.Logs;
import com.mendhak.gpslogger.loggers.FileLogger;

import org.slf4j.Logger;

public class DbLogger implements FileLogger {

    private static final Logger LOG = Logs.of(DbLogger.class);

    protected final String name = "DB";

    private Context context;

    public DbLogger(Context context) {
        this.context = context;
    }

    @Override
    public void write(Location loc) throws Exception {
        SerializableLocation sLoc = new SerializableLocation(loc);

        final Point point = new Point();
        point.setAltitude(sLoc.getAltitude());
        point.setAccuracy(sLoc.getAccuracy());
        point.setBearing(sLoc.getBearing());
        point.setLatitude(sLoc.getLatitude());
        point.setLongitude(sLoc.getLongitude());
        point.setProvider(sLoc.getProvider());
        point.setSpeed(sLoc.getSpeed());
        point.setTime(sLoc.getTime());
        point.setSatelliteCount(sLoc.getSatelliteCount());
        point.setDetectedActivity(sLoc.getDetectedActivity());

        final LocationDatabase db = LocationDatabase.getLocationDatabase(context);

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                db.pointDao().insert(point);

                return db.pointDao().countPoints();
            }

            @Override
            protected void onPostExecute(Integer param) {
                LOG.info("Total points tracked: " + param);
            }
        }.execute();
    }

    @Override
    public void annotate(String description, Location loc) throws Exception {

    }

    @Override
    public String getName() {
        return name;
    }

}
