package com.garmin.android.apps.connectiq.sample.comm.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*
@Database(entities = [HRVdata::class, PhoneUsageData::class, Locationdata::class, Accdata::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDAO(): RoomDAO

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "UserDataDB")
                .build()
    }
}
*/

@Database(entities = [Userdata::class], version = 1)
abstract class UserDatabase: RoomDatabase() {
    abstract fun userDAO(): UserDAO

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: UserDatabase? = null
        fun getInstance(context: Context): UserDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, UserDatabase::class.java, "UserDataDB")
                .build()
    }
}

@Database(entities = [Locationdata::class], version = 1)
abstract class LocationDatabase: RoomDatabase() {
    abstract fun locationDAO(): LocationDAO

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: LocationDatabase? = null
        fun getInstance(context: Context): LocationDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, LocationDatabase::class.java, "UserDataDB")
                .build()
    }
}
