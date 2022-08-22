/**
 * Copyright (C) 2015 Garmin International Ltd.
 * Subject to Garmin SDK License Agreement and Wearables Application Developer Agreement.
 */
package com.garmin.android.apps.connectiq.sample.comm.activities

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garmin.android.apps.connectiq.sample.comm.R
import com.garmin.android.apps.connectiq.sample.comm.Service.EMAService
import com.garmin.android.apps.connectiq.sample.comm.Service.FeatureService
import com.garmin.android.apps.connectiq.sample.comm.adapter.IQDeviceAdapter
import com.garmin.android.apps.connectiq.sample.comm.broadcastReceiver.EMAbroadcastReceiver
import com.garmin.android.apps.connectiq.sample.comm.roomdb.AppDatabase
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import com.garmin.android.connectiq.exception.ServiceUnavailableException
import java.util.*
import kotlin.math.pow
import kotlin.math.round


class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }

    private lateinit var connectIQ: ConnectIQ
    private lateinit var adapter: IQDeviceAdapter
    private lateinit var btnControl: Button
    private var isSdkReady = false

    private lateinit var btnTrial: Button
    private val exampleData =
        "{30s=[11], 30d=[8], 30x=[-191, -190, -292, -237, -278, -233, -209, -147, -36, 40, 74, 193, 234, 115, 18, -7, 9, 0, 0, 0, -2, -2, -1, -3, -3, -3, 0, -3, -3, 0, 1, 0, -1, 0, 0, 0, 0, 0, 0], 30y=[573, 566, 572, 570, 571, 570, 574, 571, 575, 572, 574, 573, 574, 573, 571, 575, 570, 574, 573, 576, 569, 572, 574, 573, 574, 572, 567, 540, 630, 628, 614, 563, 796, 815, 621, 540, 411, 281, 389, 437, 449, 432], 30i=[865, 915, 924, 921, 905, 860, 843, 884, 853, 857, 864, 865, 882, 900, 916, 922, 931, 954], 30z=[-861, -862, -861, -862, -861, -861, -861, -860, -864, -863, -861, -862, -1031, -901, -864, -868, -857, -855, -860, -854, -859, -860]}"
    private var dataMap1: MutableMap<String, MutableList<Int>> = mutableMapOf()
    private var dataMap2: MutableMap<String, Int> = mutableMapOf()
    private var sensorData: Map<String, MutableList<Int>> = mutableMapOf()
    private var activityData: Map<String, Int> = mutableMapOf()
    private var lastStepData: Int = -1
    private lateinit var DBhelper: AppDatabase


    private lateinit var toolbar: Toolbar

    private val connectIQListener: ConnectIQ.ConnectIQListener =
        object : ConnectIQ.ConnectIQListener {
            override fun onInitializeError(errStatus: ConnectIQ.IQSdkErrorStatus) {
                setEmptyState(getString(R.string.initialization_error) + ": " + errStatus.name)
                isSdkReady = false
            }

            override fun onSdkReady() {
                loadDevices()
                isSdkReady = true
            }

            override fun onSdkShutDown() {
                isSdkReady = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        setupUi()
        setupConnectIQSdk()

        btnControl = findViewById(R.id.btn_control)

        btnControl.setOnClickListener{
            if(isMyServiceRunning(FeatureService::class.java) && isMyServiceRunning(EMAService::class.java)){
                //현재 intervention이 실행중인 경우, 실행중인 intervention을 종료
                Toast.makeText(applicationContext, "Quit intervention", Toast.LENGTH_SHORT).show()

                val stopFeatureIntent = Intent(this, FeatureService::class.java)
                val stopEMAIntent = Intent(this, EMAService::class.java)
                stopService(stopFeatureIntent)
                stopService(stopEMAIntent)
                Log.d(TAG, "Quit intervention process")
                //connectIQ.shutdown(this)
            }
            else {
                //intervention이 실행중이지 않은 경우 Toast 메시지를 출력
                Toast.makeText(applicationContext, "No intervention is running", Toast.LENGTH_SHORT).show()
            }
        }

        btnTrial = findViewById(R.id.button)

        btnTrial.setOnClickListener{
            dataStoring(exampleData)
        }

        DBhelper = AppDatabase.getInstance(this)

        /*
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(this,EMAbroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTime = Calendar.getInstance()
        triggerTime.set(Calendar.HOUR_OF_DAY, triggerTime.get(Calendar.HOUR_OF_DAY) + 2)
        triggerTime.set(Calendar.MINUTE, 0)
        triggerTime.set(Calendar.SECOND, 0)
        triggerTime.set(Calendar.MILLISECOND, 0)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, AlarmManager.INTERVAL_HOUR, pendingIntent)
        */
    }

    public override fun onResume() {
        super.onResume()

        if (isSdkReady) {
            loadDevices()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    private fun releaseConnectIQSdk() {
        try {
            // It is a good idea to unregister everything and shut things down to
            // release resources and prevent unwanted callbacks.
            connectIQ.unregisterAllForEvents()
            connectIQ.shutdown(this)
        } catch (e: InvalidStateException) {
            // This is usually because the SDK was already shut down
            // so no worries.
        }
    }

    private fun setupUi() {
        // Setup UI.
        adapter = IQDeviceAdapter { onItemClick(it) }
        findViewById<RecyclerView>(R.id.main_recycler_view).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = this@MainActivity.adapter
        }
    }

    private fun onItemClick(device: IQDevice) {
        if(!isMyServiceRunning(FeatureService::class.java) && !isMyServiceRunning(EMAService::class.java)){
            Toast.makeText(applicationContext, "Starting Intervention...", Toast.LENGTH_SHORT).show()
            startService(FeatureService.putIntent(this, device))
            startService(EMAService.putIntent(this))
        } else {
            Toast.makeText(applicationContext, "Intervention cannot start", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "cannot start the intervention")
        }
        /*
        if(!isMyServiceRunning(InterventionService::class.java) && mPreferences.prefs.getString("isConnected", "NOT CONNECTED").equals("CONNECTED")){
            Toast.makeText(applicationContext, "Starting Intervention...", Toast.LENGTH_SHORT).show()
            startService(InterventionService.putIntent(this, device))
        } else {
            Toast.makeText(applicationContext, "Intervention cannot start", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "cannot start the intervention")
        }
        */
    }

    private fun setupConnectIQSdk() {
        // Here we are specifying that we want to use a WIRELESS bluetooth connection.
        // We could have just called getInstance() which would by default create a version
        // for WIRELESS, unless we had previously gotten an instance passing TETHERED
        // as the connection type.
        connectIQ = ConnectIQ.getInstance(this, ConnectIQ.IQConnectType.WIRELESS)

        // Initialize the SDK
        connectIQ.initialize(this, true, connectIQListener)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.load_devices -> {
                loadDevices()
                true
            }
            R.id.control_data_collection -> {
                startActivity(Intent(this, SensorActivity::class.java))
                true
            }
            R.id.intervention_ui -> {
                startActivity(Intent(this, InterventionActivity::class.java))
                true
            }
            R.id.intervention2_ui -> {
                startActivity(Intent(this, InterventionActivity2::class.java))
                true
            }
            R.id.esm_ui -> {
                startActivity(Intent(this, EMAActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadDevices() {
        try {
            // Retrieve the list of known devices.
            val devices = connectIQ.knownDevices ?: listOf()
            // OR You can use getConnectedDevices to retrieve the list of connected devices only.
            // val devices = connectIQ.connectedDevices ?: listOf()

            // Get the connectivity status for each device for initial state.
            devices.forEach {
                it.status = connectIQ.getDeviceStatus(it)
            }

            // Update ui list with the devices data
            adapter.submitList(devices)

            // Let's register for device status updates.
            devices.forEach {
                connectIQ.registerForDeviceEvents(it) { device, status ->
                    adapter.updateDeviceStatus(device, status)
                }
            }
        } catch (exception: InvalidStateException) {
            // This generally means you forgot to call initialize(), but since
            // we are in the callback for initialize(), this should never happen
        } catch (exception: ServiceUnavailableException) {
            // This will happen if for some reason your app was not able to connect
            // to the ConnectIQ service running within Garmin Connect Mobile.  This
            // could be because Garmin Connect Mobile is not installed or needs to
            // be upgraded.
            setEmptyState(getString(R.string.service_unavailable))
        }
    }



    private fun setEmptyState(text: String) {
        findViewById<TextView>(android.R.id.empty)?.text = text
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
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
            realHRVdata = Math.sqrt(receivedHRVdata)
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
            meandata = round(ACCdata.average()*100/100)
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
        return round(Math.sqrt(standardDeviation / dataList.size)*100/100)
    }

    fun calculateMAG(dataList: MutableList<Int>): Double {
        var sum = 0.0
        var mag: Double
        for (num in dataList) {
            sum += num.toDouble().pow(2)
        }
        mag = Math.sqrt(sum)
        return round(mag*100/100)
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
            /*val addRunnable1 = Runnable { lastStepData = AppDatabase.getInstance(this).userDAO().readLastStep() }
            val thread1 = Thread(addRunnable1)
            thread1.start()
            */
            if(stepdata < 0) {
                lastStepData = 0
                return 0
            }
            else {
                lastStepData = currentstepData
                return stepdata
            }
        }
        /*
        val addRunnable2 = Runnable { lastStepData = AppDatabase.getInstance(this).userDAO().readLastStep() }
        val thread2 = Thread(addRunnable2)
        thread2.start()
        */
    }

    private fun distancedataProcessing(currentdistanceData: Int?): Boolean {
        var distancechange = 0
        var lastDistanceData = 0
        if(currentdistanceData == null){
            return false
        }
        else{
            distancechange = currentdistanceData - lastDistanceData
            lastDistanceData = currentdistanceData
            if(distancechange > 1000) {
                return true
            }
            return false
        }
    }

    private fun haversineHome(avgLatitude: Double, avgLongitude: Double): Double {
        val homeLong = 36.366949
        val homeLat = 127.357542
        val dLong = Math.toRadians(avgLatitude - homeLong)
        val dLat = Math.toRadians(avgLongitude - homeLat)
        val earthRadiusKm = 6372.8

        val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLong / 2), 2.toDouble()) * Math.cos(homeLat) * Math.cos(avgLatitude);
        val c = 2 * Math.asin(Math.sqrt(a));
        return earthRadiusKm * c
    }

    private fun haversineWork(avgLatitude: Double, avgLongitude: Double): Double {
        val homeLong = 36.374233
        val homeLat = 127.365749
        val dLong = Math.toRadians(avgLatitude - homeLong)
        val dLat = Math.toRadians(avgLongitude - homeLat)
        val earthRadiusKm = 6372.8

        val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLong / 2), 2.toDouble()) * Math.cos(homeLat) * Math.cos(avgLatitude);
        val c = 2 * Math.asin(Math.sqrt(a));
        return earthRadiusKm * c
    }

    private fun dataStoring(rawData: String) {
        // DB에 한번에 저장하는 함수
        sensorData = parseSensorData(rawData).first
        activityData = parseSensorData(rawData).second
        Log.d(TAG, "Start data processing")

        val ibidata = sensorData.get("i")
        val hrvdata = ibidataProcessing(ibidata)
        Log.d(TAG, hrvdata.toString())

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
        Log.d(TAG, meanXdata.toString())
        Log.d(TAG, stdYdata.toString())
        Log.d(TAG, magZdata.toString())

        val currentstepdata = activityData.get("s")
        val stepdata = stepdataProcessing(currentstepdata)
        Log.d(TAG, stepdata.toString())

        val currentdistancedata = activityData.get("d")
        val distancedata = distancedataProcessing(currentdistancedata)
        Log.d(TAG, distancedata.toString())

        val tenbftimestamp = System.currentTimeMillis() - 10*60*1000 //10분전 timestamp
        Log.d(TAG, "${tenbftimestamp}")
        var lastLatitude = arrayOf<Double>()
        var lastLongitude = arrayOf<Double>()
        lastLatitude = AppDatabase.getInstance(this).locationDAO().readLatitudeData(tenbftimestamp)
        lastLongitude = AppDatabase.getInstance(this).locationDAO().readLongitudeData(tenbftimestamp)
        val avgLatitude = lastLatitude.average()
        Log.d(TAG, "${avgLatitude}")
        val avgLongitude = lastLongitude.average()
        Log.d(TAG, "${avgLongitude}")
        //home과의 거리
        val homeDist = haversineHome(avgLatitude, avgLongitude)
        var homedata = false
        if (homeDist < 0.1) {
            homedata = true
        }
        //work와의 거리
        val workDist = haversineWork(avgLatitude, avgLongitude)
        var workdata = false
        if (workDist < 0.1) {
            workdata = true
        }
        Log.d(TAG, homedata.toString())
        Log.d(TAG, workdata.toString())
        
        //TODO: screenTime 추가 (혜민)

        val addRunnable1 = Runnable {
            DBhelper.userDAO().insertData(System.currentTimeMillis(), 2, hrvdata,
                meanXdata, stdXdata, magXdata, meanYdata, stdYdata, magYdata, meanZdata, stdZdata, magZdata, stepdata, distancedata, homedata, workdata, 0.0)
        }
        val thread1 = Thread(addRunnable1)
        thread1.start()
    }

}