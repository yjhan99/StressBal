package com.garmin.android.apps.connectiq.sample.comm.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity
data class HRVdata(
    @PrimaryKey val current_time: String,
    @ColumnInfo(name = "HRV_data") val HRVdata: Double
)

@Entity
data class Locationdata(
    @PrimaryKey val currentTime: Long,
    @ColumnInfo(name = "LatitudeData") val LatitudeData: Double,
    @ColumnInfo(name = "LongitudeData") val LongitudeData: Double
)

@Entity
data class Userdata(
    @PrimaryKey val currentTime: Long,
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
    @ColumnInfo(name = "step") val step: Int?,
    @ColumnInfo(name = "distance") val distance: Boolean?,
    @ColumnInfo(name = "home") val home: Boolean?,
    @ColumnInfo(name = "work") val work: Boolean?,
    @ColumnInfo(name = "screenTime") val screenTime: Double?
)