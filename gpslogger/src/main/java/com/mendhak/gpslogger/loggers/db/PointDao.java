package com.mendhak.gpslogger.loggers.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PointDao {

    @Query("SELECT * FROM point")
    List<Point> getAll();

    @Query("SELECT COUNT(*) from point")
    int countPoints();

    @Insert
    void insert(Point point);

}
