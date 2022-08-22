package com.garmin.android.apps.connectiq.sample.comm.broadcastReceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.garmin.android.apps.connectiq.sample.comm.R
import com.garmin.android.apps.connectiq.sample.comm.activities.EMAActivity

class EMAbroadcastReceiver : BroadcastReceiver() {

    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        deliverNotification(context)
    }

    fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "ema_channel", // 채널의 아이디
                "ema channel", // 채널의 이름
                NotificationManager.IMPORTANCE_HIGH
                /*
                IMPORTANCE_HIGH = 알림음이 울리고 헤드업 알림으로 표시
                */
            )
            notificationChannel.enableLights(true) // 불빛
            notificationChannel.lightColor = Color.RED // 색상
            notificationChannel.enableVibration(true) // 진동 여부
            notificationChannel.description = "EMA_notification" // 채널 정보
            notificationManager.createNotificationChannel(
                notificationChannel)
        }
    }

    private fun deliverNotification(context: Context){
        val contentIntent = Intent(context, EMAActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0, // requestCode
            contentIntent, // 알림 클릭 시 이동할 인텐트
            PendingIntent.FLAG_UPDATE_CURRENT
            /*
            FLAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체
            */
        )

        val builder = NotificationCompat.Builder(context, "ema_channel")
            .setSmallIcon(R.drawable.ic_wind) // 아이콘
            .setContentTitle("EMA") // 제목
            .setContentText("Please") // 내용
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(0, builder.build())
    }
}