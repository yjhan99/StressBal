package com.garmin.android.apps.connectiq.sample.comm.roomdb

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UserDAO {
    @Query("INSERT INTO UserData VALUES (:currentTime, :label, :HRV, :meanX, :stdX, :magX, :meanY, :stdY, :magY, :meanZ, :stdZ, :magZ," +
            ":step, :distance, :home, :work)")
    fun insertData(
        currentTime: Long, label: Int, HRV: Double, meanX: Double, stdX: Double, magX: Double, meanY: Double, stdY: Double, magY: Double,
        meanZ: Double, stdZ: Double, magZ: Double, step: Int, distance: Boolean, home: Boolean, work: Boolean)

    @Query("SELECT step FROM UserData WHERE `currentTime` = (SELECT MAX(`currentTime`) FROM UserData)")
    fun readLastStep(): Int
}

@Dao
interface LocationDAO {
    @Query("INSERT INTO LocationData VALUES (:currentTime, :lat, :longt)")
    fun insertLocationData(currentTime: Long, lat: Double, longt: Double)

    @Query("DELETE FROM LocationData WHERE currentTime = :currentTime")
    fun deleteLocationData(currentTime: Long)

    @Query("SELECT LatitudeData FROM Locationdata WHERE currentTime > :currentTime")
    fun readLatitudeData(currentTime: Long): Long

    @Query("SELECT LongitudeData FROM Locationdata WHERE currentTime > :currentTime")
    fun readLongitudeData(currentTime: Long): Long
}

@Dao
interface ScreenDAO {
    @Query("INSERT INTO ScreenData VALUES (:currentTime, :screenType)")
    fun insert(currentTime: Long, screenType: String)
}