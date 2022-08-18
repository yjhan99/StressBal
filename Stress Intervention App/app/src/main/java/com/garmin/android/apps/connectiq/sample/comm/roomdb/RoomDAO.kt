package com.garmin.android.apps.connectiq.sample.comm.roomdb

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UserDAO {
    @Query("INSERT INTO Userdata VALUES (:currentTime, :label, :HRV, :meanX, :stdX, :magX, :meanY, :stdY, :magY, :meanZ, :stdZ, :magZ," +
            ":step, :distance, :home, :work, :screenTime)")
    fun insertData(currentTime: Long, label: Int, HRV: Double, meanX: Double, stdX: Double, magX: Double, meanY: Double, stdY: Double, magY: Double,
                   meanZ: Double, stdZ: Double, magZ: Double, step: Int, distance: Boolean, home: Boolean, work: Boolean, screenTime: Double)

    @Query("SELECT step FROM Userdata WHERE `currentTime` = (SELECT MAX(`currentTime`) FROM Userdata)")
    fun readLastStep(): Int
}

@Dao
interface LocationDAO {
    @Query("INSERT INTO Locationdata VALUES (:currentTime, :lat, :longt)")
    fun insertLocationData(currentTime: Long, lat: Double, longt: Double)

    @Query("DELETE FROM LOCATIONDATA WHERE currentTime = :currentTime")
    fun deleteLocationData(currentTime: Long)

    @Query("SELECT LatitudeData FROM Locationdata WHERE currentTime > :currentTime")
    fun readLatitudeData(currentTime: Long): Array<Double>

    @Query("SELECT LongitudeData FROM Locationdata WHERE currentTime > :currentTime")
    fun readLongitudeData(currentTime: Long): Array<Double>
}