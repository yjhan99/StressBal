package com.garmin.android.apps.connectiq.sample.comm.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.util.concurrent.ForkJoinWorkerThread

@Dao
interface UserDAO {
    @Query("INSERT INTO Userdata VALUES (:currentTime, :label, :HRV, :meanX, :stdX, :magX, :meanY, :stdY, :magY, :meanZ, :stdZ, :magZ," +
            ":step, :distance, :home, :work, :screenTime)")
    fun insertData(currentTime: String, label: Int, HRV: Double, meanX: Double, stdX: Double, magX: Double, meanY: Double, stdY: Double, magY: Double,
                   meanZ: Double, stdZ: Double, magZ: Double, step: Double, distance: Boolean, home: Boolean, work: Boolean, screenTime: Double)

    @Query("SELECT step FROM Userdata WHERE `currentTime` = (SELECT MAX(`currentTime`) FROM Userdata)")
    fun readLastStep(): Int
}

@Dao
interface LocationDAO {
    @Query("INSERT INTO Locationdata VALUES (:currentTime, :lat, :longt)")
    fun insertLocationData(currentTime: String, lat: Double, longt: Double)

    @Query("DELETE FROM Locationdata WHERE current_time = :currentTime")
    fun deleteLocationData(currentTime: String)
}

@Dao
interface ScreenDAO {
    @Insert
    fun insert(screenData: ScreenData)

    @Delete
    fun delete(key: String)
}