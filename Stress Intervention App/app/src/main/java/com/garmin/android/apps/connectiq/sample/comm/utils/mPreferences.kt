package com.garmin.android.apps.connectiq.sample.comm.utils

import android.app.Application

class mPreferences: Application() {
    companion object {
        lateinit var prefs: PreferencesUtil
    }

    override fun onCreate() {
        prefs = PreferencesUtil(applicationContext)
        super.onCreate()
    }
}