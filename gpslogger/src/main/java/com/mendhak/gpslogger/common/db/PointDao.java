package com.mendhak.gpslogger.common.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PointDao {

    @Query("SELECT * FROM point")
    List<Point> getAll();

    @Query("SELECT COUNT(*) FROM point")
    int countPoints();

    @Query("SELECT * FROM point where published = 0 order by id asc limit 50")
    List<Point> getUnpublished();

    @Query("UPDATE point SET published = 1 WHERE id = :id")
    void publish(int id);

    @Insert
    void insert(Point point);

}
