package com.garmin.android.apps.connectiq.sample.comm.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity
data class HRVdata(
    @PrimaryKey val currentTime: String,
    @ColumnInfo(name = "HRVdata") val HRVdata: Double
)

@Entity
data class Locationdata(
    @PrimaryKey val currentTime: String,
    @ColumnInfo(name = "LatitudeData") val LatitudeData: Double,
    @ColumnInfo(name = "LongtitudData") val LongtitudeData: Double
)

@Entity
data class Userdata(
    @PrimaryKey val currentTime: String,
    @ColumnInfo(name = "label") var label: Int?,
    @ColumnInfo(name = "HRV") val HRV: Double?,
    @ColumnInfo(name = "meanX") val meanX: Double?,
    @ColumnInfo(name = "stdX") val stdX: Double?,
    @ColumnInfo(name = "magX") val magX: Double?,
    @ColumnInfo(name = "meanY") val meanY: Double?,
    @ColumnInfo(name = "stdY") val stdY: Double?,
    @ColumnInfo(name = "magY") val magY: Double?,
    @ColumnInfo(name = "meanZ") val meanZ: Double?,
    @ColumnInfo(name = "stdZ") val stdZ: Double?,
    @ColumnInfo(name = "magZ") val magZ: Double?,
    @ColumnInfo(name = "step") val step: Double?,
    @ColumnInfo(name = "distance") val distance: Boolean?,
    @ColumnInfo(name = "home") val home: Boolean?,
    @ColumnInfo(name = "work") val work: Boolean?
)

@Entity
data class ScreenData(
    @PrimaryKey val currentTime: String,
    @ColumnInfo var eventType: String
){
    constructor(eventType: String): this(Timestamp(System.currentTimeMillis()).toString() ,eventType)
}