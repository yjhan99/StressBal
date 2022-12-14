package com.garmin.android.apps.connectiq.sample.comm2.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.garmin.android.apps.connectiq.sample.comm2.R
import com.garmin.android.apps.connectiq.sample.comm2.roomdb.AppDatabase
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import java.lang.Math.sqrt
import kotlin.math.pow
import kotlin.math.round


class FeatureService : Service() {

    companion object {
        private const val TAG = "FeatureService"
        private const val EXTRA_IQ_DEVICE = "IQDevice"
        private const val COMM_WATCH_ID = "5d80e574-aa63-4fae-8dc0-f58656071277"

        fun putIntent(context: Context, device: IQDevice?): Intent {
            val intent = Intent(context, FeatureService::class.java)
            intent.putExtra(EXTRA_IQ_DEVICE, device)
            return intent
        }
    }

    private val connectIQ: ConnectIQ = ConnectIQ.getInstance()
    private lateinit var device: IQDevice
    private lateinit var myApp: IQApp

    private lateinit var notificationManager: NotificationManager
    private val GROUP_KEY_NOTIFY = "group_key_notify"

    private var dataMap1: MutableMap<String, MutableList<Int>> = mutableMapOf()
    private var dataMap2: MutableMap<String, Int> = mutableMapOf()
    private var sensorData: Map<String, MutableList<Int>> = mutableMapOf()
    private var activityData: Map<String, Int> = mutableMapOf()
    private var lastStepData: Int = -1
    private var lastDistanceData: Int = -1

    //private lateinit var DBhelper: AppDatabase

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //μλΉμ€λ νλ² μ€νλλ©΄ κ³μ μ€νλ μνλ‘ μ μ§λκΈ° λλ¬Έμ onCreate()μμ intentλ₯Ό λ°μ μ²λ¦¬νκΈ°μλ μ μ νμ§ μμ
        //λ°λΌμ intentμ λν μ²λ¦¬λ onStartCommand()μμ μν

        // Notification μ€μ 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Stress Intervention"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel("intervention_channel", name, importance)

            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        } else {
            //Oreo μ΄νμμμ μ²λ¦¬
        }

        val builder = NotificationCompat.Builder(this, "intervention_channel")
            .setSmallIcon(R.drawable.ic_wind)
            .setGroup(GROUP_KEY_NOTIFY)
            .setAutoCancel(false)
        startForeground(Constants.INTERVENTION_SERVICE_ID, builder.build())

        //DBhelper = AppDatabase.getInstance(this)
        //νμ¬ μμ΄ μν... μ€λ₯λ  κ²½μ° νμΈ

    }

    private fun listenByMyAppEvents() {
        try {
            Log.d(TAG, "registering garmin app events...")
            connectIQ.registerForAppEvents(device, myApp) { device, app, message, status ->
                // We know from our Comm sample widget that it will only ever send us strings, but in case
                // we get something else, we are simply going to do a toString() on each object in the
                // message list.
                val builder = StringBuilder()
                if (message.size > 0) {
                    for (o in message) {
                        builder.append(o.toString())
                        builder.append("\r\n")
                    }
                    Log.d(TAG, "Received data from Garmin Watch: $builder")
                    //giveFeedback(builder.toString())
                    dataStoring(builder.toString())
                } else {
                    builder.append("Received an empty message from the application")
                }
            }
        } catch (e: InvalidStateException) {
            Log.e(TAG, "ConnectIQ is not in a valid state")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null || !intent.hasExtra(EXTRA_IQ_DEVICE)){
            Log.d(TAG, "there is no intent")
            //return START_REDELIVER_INTENT //μλΉμ€κ° μ’λ£λμ΄λ μλμΌλ‘ μ΄μ  intent μ λ³΄λ₯Ό κ°μ§κ³  λ€μ μ€ν
            return START_NOT_STICKY //μμνκΈ°μ μΆ©λΆν μ λ³΄κ° λμ΄μ€μ§ μμ κ²½μ° μ¬μμ μμ΄ μλΉμ€ μ’λ£
        } else {
            // intentκ° μ‘΄μ¬νλ©°, μλΉμ€κ° μμλμ§ μμ κ²½μ° λ μ°κ²°λ κΈ°κΈ° μ΄λ¦ μ λ³΄ νμ±
            device = intent.getParcelableExtra<Parcelable>(EXTRA_IQ_DEVICE) as IQDevice
            myApp = IQApp(COMM_WATCH_ID)
            Log.d(TAG, "connected Device: " + device.friendlyName)
            listenByMyAppEvents()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        // TODO : μλΉμ€ μ’λ£μ ν  κ²λ€(νμ¬ shutdown..μ νλ κ²½μ° μ΅μμ λ°μ
        try{
            if(this::device.isInitialized && this::myApp.isInitialized){
                connectIQ.unregisterForDeviceEvents(device)
                connectIQ.unregisterForApplicationEvents(device, myApp)
                connectIQ.unregisterAllForEvents()
            }
            //connectIQ.shutdown(applicationContext)
        } catch (e: InvalidStateException) {
            Log.e(TAG, e.toString())
        }

        super.onDestroy()
        Log.d(TAG, "Service execution finished")
    }

    private fun parseSensorData(rawDatas: String): Pair<Map<String, MutableList<Int>>, Map<String, Int>> {
        try {
            var dataName: String = String()
            var dataList = rawDatas.split("=", "], ")
            dataList.forEach{
                if (it.contains("i") || it.contains("x") || it.contains("y") || it.contains("z") || it.contains("s") || it.contains("d")) {
                    dataName = it.last().toString()
                    //return@forEach
                }
                else {
                    if (it.contains(",")) {
                        if (it.contains("]")) {
                            dataMap1.put(
                                dataName,
                                it.substring(it.indexOf("[") + 1, it.indexOf("]")).replace(" ", "").split(",").map{it.toInt()} as MutableList<Int>)
                        }
                        else{
                            dataMap1.put(
                                dataName,
                                it.substring(it.indexOf("[") + 1).replace(" ", "").split(",").map{it.toInt()} as MutableList<Int>)
                        }
                    }
                    else{
                        if(it.contains("]")) {
                            dataMap2.put(
                                dataName,
                                it.substring(it.indexOf("[") + 1, it.indexOf("]")).toInt())
                        }
                        else {
                            dataMap2.put(
                                dataName,
                                it.substring(it.indexOf(("[")) + 1).toInt())
                        }
                    }
                }
            }
            return Pair(dataMap1, dataMap2)
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, e.toString())
        } catch (e: NumberFormatException){
            Log.e(TAG, e.toString())
        }
        return Pair(mapOf("i" to mutableListOf(), "x" to mutableListOf(), "y" to mutableListOf(), "z" to mutableListOf()),
            mapOf("s" to 0, "d" to 0))
    }

    private fun ibidataProcessing(IBIdata: MutableList<Int>?): Double {
        var receivedHRVdata = 0.0
        var realHRVdata: Double
        if(IBIdata.isNullOrEmpty()){
            realHRVdata = 0.0
            return realHRVdata
        }
        else{
            for(i in 0 until (IBIdata.size-1)){
                receivedHRVdata += (IBIdata[i+1] - IBIdata[i]).toDouble().pow(2.0)
            }
            if(IBIdata.size > 1){
                receivedHRVdata /= (IBIdata.size-1)
            }
            realHRVdata = sqrt(receivedHRVdata)
            return round(realHRVdata*100)/100
        }
    }

    private fun accdataProcessing(ACCdata: MutableList<Int>?): Triple<Double, Double, Double> {
        val meandata: Double
        val stddata: Double
        val magdata: Double
        if(ACCdata.isNullOrEmpty()){
            return Triple(0.0, 0.0, 0.0)
        }
        else{
            meandata = round(ACCdata.average()*100)/100
            stddata = calculateSD(ACCdata)
            magdata = calculateMAG(ACCdata)
            return Triple(meandata, stddata, magdata)
        }
    }

    fun calculateSD(dataList: MutableList<Int>): Double {
        var sum = 0.0
        var standardDeviation = 0.0
        for (num in dataList) {
            sum += num
        }
        val mean = sum / dataList.size
        for (num in dataList) {
            standardDeviation += Math.pow(num - mean, 2.0)
        }
        return round(sqrt(standardDeviation / dataList.size)*100)/100
    }

    fun calculateMAG(dataList: MutableList<Int>): Double {
        var sum = 0.0
        var mag: Double
        for (num in dataList) {
            sum += num.toDouble().pow(2)
        }
        mag = sqrt(sum)
        return round(mag*100)/100
    }

    private fun stepdataProcessing(currentstepData: Int?): Int {
        var stepdata = 0
        if(currentstepData == null){
            return 0
        }
        else{
            if(lastStepData == -1) {
                lastStepData = currentstepData
                return 0
            }
            stepdata = currentstepData - lastStepData
            if(stepdata < 0) {
                return 0
            }
            else {
                lastStepData = currentstepData
                return stepdata
            }
        }
    }

    private fun distancedataProcessing(currentdistanceData: Int?): Boolean {
        var distancechange = 0
        if(currentdistanceData == null){
            return false
        }
        else{
            if(lastDistanceData == -1) {
                lastDistanceData = currentdistanceData
                return false
            }
            else {
                distancechange = currentdistanceData - lastDistanceData
                if(distancechange < 0) {
                    return false
                }
                lastDistanceData = currentdistanceData
                if(distancechange > 10) {
                    return true
                }
                return false
            }
        }
    }

    private fun haversineHome(avgLatitude: Double, avgLongitude: Double): Double {
        val homeLong = 36.366949
        val homeLat = 127.357542
        val dLong = Math.toRadians(avgLatitude - homeLong)
        val dLat = Math.toRadians(avgLongitude - homeLat)
        val earthRadiusKm = 6372.8

        val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLong / 2), 2.toDouble()) * Math.cos(homeLat) * Math.cos(avgLatitude);
        val c = 2 * Math.asin(sqrt(a));
        return earthRadiusKm * c
    }

    private fun haversineWork(avgLatitude: Double, avgLongitude: Double): Double {
        val homeLong = 36.374233
        val homeLat = 127.365749
        val dLong = Math.toRadians(avgLatitude - homeLong)
        val dLat = Math.toRadians(avgLongitude - homeLat)
        val earthRadiusKm = 6372.8

        val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLong / 2), 2.toDouble()) * Math.cos(homeLat) * Math.cos(avgLatitude);
        val c = 2 * Math.asin(sqrt(a));
        return earthRadiusKm * c
    }

    private fun dataStoring(rawData: String) {
        // DBμ νλ²μ μ μ₯νλ ν¨μ
        Log.d(TAG, "Start data processing")
        sensorData = parseSensorData(rawData).first
        activityData = parseSensorData(rawData).second

        val ibidata = sensorData.get("i")
        val hrvdata = ibidataProcessing(ibidata)

        val accXdata = sensorData.get("x")
        val meanXdata = accdataProcessing(accXdata).first
        val stdXdata = accdataProcessing(accXdata).second
        val magXdata = accdataProcessing(accXdata).third
        val accYdata = sensorData.get("y")
        val meanYdata = accdataProcessing(accYdata).first
        val stdYdata = accdataProcessing(accYdata).second
        val magYdata = accdataProcessing(accYdata).third
        val accZdata = sensorData.get("z")
        val meanZdata = accdataProcessing(accZdata).first
        val stdZdata = accdataProcessing(accZdata).second
        val magZdata = accdataProcessing(accZdata).third

        val currentstepdata = activityData.get("s")
        val stepdata = stepdataProcessing(currentstepdata)

        val currentdistancedata = activityData.get("d")
        val distancedata = distancedataProcessing(currentdistancedata)

        val tenbftimestamp = System.currentTimeMillis() - 10*60*1000 //10λΆμ  timestamp
        val lastLatitude = AppDatabase.getInstance(this).locationDAO().readLatitudeData(tenbftimestamp)
        val lastLongitude = AppDatabase.getInstance(this).locationDAO().readLongitudeData(tenbftimestamp)
        val avgLatitude = lastLatitude.average()
        val avgLongitude = lastLongitude.average()
        //homeκ³Όμ κ±°λ¦¬
        val homeDist = haversineHome(avgLatitude, avgLongitude)
        var homedata = false
        if (homeDist < 0.1) {
            homedata = true
        }
        //workμμ κ±°λ¦¬
        val workDist = haversineWork(avgLatitude, avgLongitude)
        var workdata = false
        if (workDist < 0.1) {
            workdata = true
        }

        //screentime κ³μ°
        val screendata = AppDatabase.getInstance(this).screenDAO().readScreenData(tenbftimestamp)
        var beforetime = tenbftimestamp
        var aftertime = tenbftimestamp
        var screentime = 0.0
        if (screendata.isNullOrEmpty()) {
            val recentScreenData = AppDatabase.getInstance(this).screenDAO().readRecentScreenData()
            if (recentScreenData == "Screen On") {
                screentime = 300.0
            }
            else if (recentScreenData == "Screen Off") {
                screentime = 0.0
            }
        } else {
            screendata.forEach {
                if (it.eventType == "Screen On") {
                    beforetime = it.currentTime
                } else if (it.eventType == "Screen Off") {
                    aftertime = it.currentTime
                    screentime += aftertime - beforetime
                }
            }
            if (screendata.last().eventType == "Screen On") {
                screentime += System.currentTimeMillis() - beforetime
            }
            screentime = screentime/1000
        }

        if (hrvdata != 0.0) {
            val addRunnable = Runnable {
                AppDatabase.getInstance(this).userDAO().insertData(System.currentTimeMillis(), 2, hrvdata,
                    meanXdata, stdXdata, magXdata, meanYdata, stdYdata, magYdata, meanZdata, stdZdata, magZdata, stepdata, distancedata, homedata, workdata, screentime)
            }
            val thread = Thread(addRunnable)
            thread.start()
            Log.d(TAG, "Time: ${System.currentTimeMillis().toString()} HRV: ${hrvdata} meanX: ${meanXdata} stdX: ${stdXdata} magX: ${magXdata} " +
                    "meanY: ${meanYdata} stdY: ${stdYdata} magZ: ${magZdata} meanZ: ${meanZdata} stdZ: ${stdZdata} magX: ${magZdata} " +
                    "step: ${stepdata} distance: ${distancedata} home: ${homedata} work: ${workdata} screen: ${screentime}")
        }
    }



}