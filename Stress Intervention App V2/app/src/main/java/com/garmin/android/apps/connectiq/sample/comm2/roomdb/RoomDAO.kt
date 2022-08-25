package com.garmin.android.apps.connectiq.sample.comm2.roomdb

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UserDAO {
    @Query("INSERT INTO Userdata VALUES (:currentTime, :label, :HRV, :meanX, :stdX, :magX, :meanY, :stdY, :magY, :meanZ, :stdZ, :magZ," +
            ":step, :distance, :home, :work, :screenTime)")
    fun insertData(currentTime: Long, label: Int, HRV: Double, meanX: Double, stdX: Double, magX: Double, meanY: Double, stdY: Double, magY: Double,
                   meanZ: Double, stdZ: Double, magZ: Double, step: Int, distance: Boolean, home: Boolean, work: Boolean, screenTime: Double)

    @Query("SELECT step FROM Userdata WHERE currentTime = (SELECT MAX(currentTime) FROM Userdata)")
    fun readLastStep(): Int

    @Query("UPDATE Userdata SET label = (:emaResult) WHERE currentTime >= (:minus10Time) AND currentTime <= (:plus10Time)")
    fun updateEMAResult(emaResult:Int, minus10Time: Long, plus10Time: Long)
}

@Dao
interface LocationDAO {
    @Query("INSERT INTO Locationdata VALUES (:currentTime, :lat, :longt)")
    fun insertLocationData(currentTime: Long, lat: Double, longt: Double)

    @Query("DELETE FROM LOCATIONDATA WHERE currentTime = :currentTime")
    fun deleteLocationData(currentTime: Long)

    @Query("SELECT LatitudeData FROM Locationdata WHERE currentTime > (:lastTime)")
    fun readLatitudeData(lastTime: Long): Array<Double>

    @Query("SELECT LongitudeData FROM Locationdata WHERE currentTime > (:lastTime)")
    fun readLongitudeData(lastTime: Long): Array<Double>
}

@Dao
interface ScreenDAO {
    @Query("INSERT INTO ScreenData VALUES (:currentTime, :screenType)")
    fun insert(currentTime: Long, screenType: String)

    @Query("SELECT eventType FROM ScreenData WHERE currentTime = (SELECT MAX(currentTime) FROM Userdata) ")
    fun readRecentScreenData(): String

    @Query("SELECT * FROM ScreenData WHERE currentTime > (:lastTime)")
    fun readScreenData(lastTime: Long): Array<ScreenData>
}

@Dao
interface LabelDAO {
    @Query("INSERT INTO Labeldata VALUES (:currentTime, :label)")
    fun insertLabelData(currentTime: Long, label: Int)

    @Query("SELECT * FROM Labeldata WHERE currentTime > (:lastTime)")
    fun readLabelData(lastTime: Long): Array<Labeldata>
}