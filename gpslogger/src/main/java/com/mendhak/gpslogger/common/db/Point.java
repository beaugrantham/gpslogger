package com.mendhak.gpslogger.common.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Point {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "altitude")
    private double altitude;

    @ColumnInfo(name = "accuracy")
    private double accuracy;

    @ColumnInfo(name = "bearing")
    private float bearing;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "provider")
    private String provider;

    @ColumnInfo(name = "speed")
    private float speed;

    @ColumnInfo(name = "time")
    private long time;

    @ColumnInfo(name = "satellite_count")
    private int satelliteCount;

    @ColumnInfo(name = "detected_activity")
    private String detectedActivity;

    @ColumnInfo(name = "published")
    private boolean published;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getSatelliteCount() {
        return satelliteCount;
    }

    public void setSatelliteCount(int satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    public String getDetectedActivity() {
        return detectedActivity;
    }

    public void setDetectedActivity(String detectedActivity) {
        this.detectedActivity = detectedActivity;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

}
