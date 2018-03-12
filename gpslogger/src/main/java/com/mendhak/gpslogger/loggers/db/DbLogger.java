package com.mendhak.gpslogger.loggers.db;

import android.location.Location;

import com.mendhak.gpslogger.GpsLoggingService;
import com.mendhak.gpslogger.common.slf4j.Logs;
import com.mendhak.gpslogger.loggers.FileLogger;

import org.slf4j.Logger;

/**
 * Created by Beau on 3/12/2018.
 */

public class DbLogger implements FileLogger {

    private static final Logger LOG = Logs.of(DbLogger.class);

    protected final String name = "DB";

    @Override
    public void write(Location loc) throws Exception {

    }

    @Override
    public void annotate(String description, Location loc) throws Exception {

    }

    @Override
    public String getName() {
        return name;
    }

}
