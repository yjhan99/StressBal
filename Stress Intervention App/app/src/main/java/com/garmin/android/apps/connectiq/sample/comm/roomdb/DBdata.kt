package com.garmin.android.apps.connectiq.sample.comm.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HRVdata(
    @PrimaryKey val current_time: String,
    @ColumnInfo(name = "HRV_data") val HRVdata: Double
)

@Entity
data class PhoneUsageData(
    val current_time: String,
    @ColumnInfo(name = "Package_Name") val PackageName: String,
    @ColumnInfo(name = "LastTime_Used") val LastTimeUsed: String,
    @ColumnInfo(name = "TotalTime_Foreground") val TotalTimeInForeground: Long,
    @PrimaryKey(autoGenerate = true) val PUID: Int = 0
)

@Entity
data class Accdata(
    @PrimaryKey val current_time: String,
    @ColumnInfo(name = "Acc_X_data") val AccXdata: Float,
    @ColumnInfo(name = "Acc_Y_data") val AccYdata: Float,
    @ColumnInfo(name = "Acc_Z_data") val AccZdata: Float
)

@Entity
data class Locationdata(
    @PrimaryKey val current_time: String,
    @ColumnInfo(name = "Latitude_data") val Latitudedata: Double,
    @ColumnInfo(name = "Longtitude_data") val Longtitudedata: Double
)

@Entity
data class Userdata(
    @PrimaryKey val current_time: String,
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
    @ColumnInfo(name = "work") val work: Boolean?,
    @ColumnInfo(name = "screenTime") val screenTime: Double?
)