package com.garmin.android.apps.connectiq.sample.comm.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.util.concurrent.ForkJoinWorkerThread

/*
@Dao
interface RoomDAO {
    @Query("SELECT * FROM HRVdata")
    fun getAllHRVdata(): List<HRVdata>

    @Query("SELECT * FROM HRVdata WHERE current_time LIKE :currentTime")
    fun findHRVdataByTime(currentTime: String): HRVdata

    @Query("INSERT INTO HRVdata VALUES (:currentTime, :HRVdata)")
    fun insertHRVdata(currentTime: String, HRVdata: Double)

    @Query("DELETE FROM HRVdata WHERE current_time = :currentTime")
    fun deleteHRVdata(currentTime: String)

    @Query("INSERT INTO PhoneUsageData VALUES (:currentTime, :packName, :lastTimeUsed, :totalTime, null)")
    fun insertPhoneUsageData(currentTime: String, packName:String, lastTimeUsed: String, totalTime: Long)

    @Query("DELETE FROM PhoneUsageData WHERE (current_time = :currentTime AND Package_Name = :packName)")
    fun deletePhoneUsageData(currentTime: String, packName: String)

    @Query("INSERT INTO Locationdata VALUES (:currentTime, :lat, :longt)")
    fun insertLocationData(currentTime: String, lat: Double, longt: Double)

    @Query("DELETE FROM Locationdata WHERE current_time = :currentTime")
    fun deleteLocationData(currentTime: String)

    @Query("INSERT INTO Accdata VALUES (:currentTime, :x, :y, :z)")
    fun insertAccData(currentTime: String, x: Float, y: Float, z: Float)

    @Query("DELETE FROM Accdata WHERE current_time = :currentTime")
    fun deleteAccData(currentTime: String)
}
*/

@Dao
interface UserDAO {
    @Query("INSERT INTO Userdata VALUES (:currentTime, :label, :HRV, :meanX, :stdX, :magX, :meanY, :stdY, :magY, :meanZ, :stdZ, :magZ," +
            ":step, :distance, :home, :work, :screenTime)")
    fun insertData(currentTime: String, label: Int, HRV: Double, meanX: Double, stdX: Double, magX: Double, meanY: Double, stdY: Double, magY: Double,
                   meanZ: Double, stdZ: Double, magZ: Double, step: Double, distance: Boolean, home: Boolean, work: Boolean, screenTime: Double)

    @Query("SELECT step FROM Userdata WHERE `current_time` = (SELECT MAX(`current_time`) FROM Userdata)")
    fun readLastStep(): Int
}

@Dao
interface LocationDAO {
    @Query("INSERT INTO Locationdata VALUES (:currentTime, :lat, :longt)")
    fun insertLocationData(currentTime: String, lat: Double, longt: Double)

    @Query("DELETE FROM Locationdata WHERE current_time = :currentTime")
    fun deleteLocationData(currentTime: String)
}