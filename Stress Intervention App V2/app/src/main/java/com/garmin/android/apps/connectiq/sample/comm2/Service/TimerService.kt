package com.garmin.android.apps.connectiq.sample.comm2.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.garmin.android.apps.connectiq.sample.comm2.R
import com.garmin.android.apps.connectiq.sample.comm2.activities.EMAActivity
import com.garmin.android.apps.connectiq.sample.comm2.activities.InterventionActivity
import com.garmin.android.apps.connectiq.sample.comm2.model.Classifier
import com.garmin.android.apps.connectiq.sample.comm2.roomdb.AppDatabase
import java.io.IOException
import java.util.*

class TimerService: Service() {

    companion object {
        private const val TAG = "TimerService"

        fun putIntent(context: Context): Intent {
            val intent = Intent(context, TimerService::class.java)
            return intent
        }
    }

    private lateinit var notificationManager: NotificationManager
    private val GROUP_KEY_NOTIFY = "group_key_notify"

    private var iteration = 0

    private lateinit var classifier: Classifier
    private var modelResult = 0

    private var timer = Timer()
    private var timerTask = object : TimerTask() {
        override fun run() {
            // TODO: 가장 최근의 데이터만 사용함 (10분전으로 바꿔야)
            val recentData = AppDatabase.getInstance(this@TimerService).userDAO().readRecentData()
            var inputData = FloatArray(15)
            if(recentData == null) {
                Log.d(TAG,"There is no recent data")
            }
            else {
                Log.d(TAG, "input data start")
                inputData.plus(recentData.HRV!!.toFloat())
                inputData.plus(recentData.meanX!!.toFloat())
                inputData.plus(recentData.stdX!!.toFloat())
                inputData.plus(recentData.magX!!.toFloat())
                inputData.plus(recentData.meanY!!.toFloat())
                inputData.plus(recentData.stdY!!.toFloat())
                inputData.plus(recentData.magY!!.toFloat())
                inputData.plus(recentData.meanZ!!.toFloat())
                inputData.plus(recentData.stdZ!!.toFloat())
                inputData.plus(recentData.magZ!!.toFloat())
                inputData.plus(recentData.step!!.toFloat())
                var distance = if (recentData.distance == true) 1F else 0F
                inputData.plus(distance)
                var home = if (recentData.home == true) 1F else 0F
                inputData.plus(home)
                var work = if (recentData.work == true) 1F else 0F
                inputData.plus(work)
                inputData.plus(recentData.currentTime!!.toFloat())

                modelResult = classifier.classify(inputData).first
                Log.d(TAG, "${modelResult}")
            }

            if (iteration < 5) {
                if (modelResult == 0) {
                    interventionNotification()
                    Log.d(TAG, "Intervention Sent")
                }
            }
            else {
                emaNotification()
                iteration = 0
            }
            iteration += 1
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "EMA Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel("ema_channel", name, importance)

            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        } else {
            //Oreo 이하에서의 처리
        }

        val builder = NotificationCompat.Builder(this, "intervention_channel")
            .setSmallIcon(R.drawable.ic_wind)
            .setGroup(GROUP_KEY_NOTIFY)
            .setAutoCancel(false)
        startForeground(Constants.EMA_SERVICE_ID, builder.build())

        initClassifier()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null){
            Log.d(TAG, "there is no intent")
            return START_NOT_STICKY
        } else {
            Log.d(TAG, "EMA service started...")
            //timer.scheduleAtFixedRate(timerTask, 1200000, 1200000)
            timer.scheduleAtFixedRate(timerTask, 600000, 600000)

        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun emaNotification() {
        //notification 보내기
        val notificationIntent = Intent(this, EMAActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "ema_channel")
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_ema)
            .setContentText("Please answer the question")
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY_NOTIFY)

        notificationManager.notify(1, builder.build())
        Log.d(TAG, "Notification Sent")

        //화면 켜기
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val wLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "myapp:TAG")
        if(wLock != null && !wLock.isHeld){
            wLock.acquire(3*1000L /*3 seconds*/)
        }

        //진동 설정(0.5초 진동)
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    private fun interventionNotification() {
        //notification 보내기
        val notificationIntent = Intent(this, InterventionActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "ema_channel")
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_ema)
            .setContentText("Hey :)")
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY_NOTIFY)
            .setTimeoutAfter(60000)

        notificationManager.notify(2, builder.build())
        Log.d(TAG, "Notification Sent")

        //화면 켜기
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val wLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "myapp:TAG")
        if(wLock != null && !wLock.isHeld){
            wLock.acquire(3*1000L /*3 seconds*/)
        }

        //진동 설정(0.5초 진동)
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    private fun initClassifier() {
        classifier = Classifier(assets, Classifier.STRESS_CLASSIFIER)
        try {
            classifier.init()
        } catch (exception: IOException) {
            Log.d(TAG, "IOException")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::classifier.isInitialized) classifier.finish()
        timer.cancel()
    }
}