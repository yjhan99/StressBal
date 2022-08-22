package com.garmin.android.apps.connectiq.sample.comm.Service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class UpdateService: Service() {

    companion object {
        private const val TAG = "UpdateService"

        fun putIntent(context: Context): Intent {
            val intent = Intent(context, UpdateService::class.java)
            return intent
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}