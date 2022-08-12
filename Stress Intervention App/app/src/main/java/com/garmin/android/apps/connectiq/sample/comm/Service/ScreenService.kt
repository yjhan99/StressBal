package com.garmin.android.apps.connectiq.sample.comm.Service

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.garmin.android.apps.connectiq.sample.comm.activities.SensorActivity
import com.garmin.android.apps.connectiq.sample.comm.roomdb.AppDatabase
import com.garmin.android.apps.connectiq.sample.comm.roomdb.ScreenData

class ScreenService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val channelId = "screen_notification_channel"
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent(this, SensorActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentTitle("Screen Service")
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    "Screen Service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "This channel is used by screen service"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        startForeground(Constants.SCREEN_SERVICE_ID ,builder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent == null){
            return START_NOT_STICKY //시작하기에 충분한 정보가 넘어오지 않은 경우 재시작 없이 서비스 종료
        }

        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)
        val receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent!!.action
                var screenType = ""
                when (action) {
                    Intent.ACTION_SCREEN_ON -> {
                        screenType = "Screen On"
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        screenType = "Screen Off"
                    }
                }
                val addRunnable = Runnable {
                    AppDatabase.getInstance(applicationContext).screenDAO().insert(ScreenData(screenType))
                }
                val thread = Thread(addRunnable)
                thread.start()
            }
        }

        registerReceiver(receiver, intentFilter)
        return START_NOT_STICKY
    }
}