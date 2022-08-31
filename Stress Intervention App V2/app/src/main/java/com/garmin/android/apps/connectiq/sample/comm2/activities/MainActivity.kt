/**
 * Copyright (C) 2015 Garmin International Ltd.
 * Subject to Garmin SDK License Agreement and Wearables Application Developer Agreement.
 */
package com.garmin.android.apps.connectiq.sample.comm2.activities

import android.app.ActivityManager
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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.garmin.android.apps.connectiq.sample.comm2.R
import com.garmin.android.apps.connectiq.sample.comm2.Service.TimerService
import com.garmin.android.apps.connectiq.sample.comm2.Service.FeatureService
import com.garmin.android.apps.connectiq.sample.comm2.UpdateWorker
import com.garmin.android.apps.connectiq.sample.comm2.adapter.IQDeviceAdapter
import com.garmin.android.apps.connectiq.sample.comm2.model.Classifier
import com.garmin.android.apps.connectiq.sample.comm2.model.Classifier2
import com.garmin.android.apps.connectiq.sample.comm2.model.Updater
import com.garmin.android.apps.connectiq.sample.comm2.roomdb.AppDatabase
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import com.garmin.android.connectiq.exception.ServiceUnavailableException
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }

    private lateinit var connectIQ: ConnectIQ
    private lateinit var adapter: IQDeviceAdapter
    private lateinit var btnControl: Button
    private var isSdkReady = false

    private lateinit var toolbar: Toolbar

    private lateinit var btn: Button
    private lateinit var updater: Updater
    private lateinit var classifier2: Classifier2

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

        initUpdater()
        initClassifier2()

        btnControl = findViewById(R.id.btn_control)

        btnControl.setOnClickListener{
            if(isMyServiceRunning(FeatureService::class.java) && isMyServiceRunning(TimerService::class.java)){
                //현재 intervention이 실행중인 경우, 실행중인 intervention을 종료
                Toast.makeText(applicationContext, "Quit intervention", Toast.LENGTH_SHORT).show()

                val stopFeatureIntent = Intent(this, FeatureService::class.java)
                val stopEMAIntent = Intent(this, TimerService::class.java)
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

        btn = findViewById(R.id.button)

        btn.setOnClickListener {
            //val tenbftimestamp = System.currentTimeMillis() - 10*60*1000
            val recentData = AppDatabase.getInstance(this).userDAO().readRecentData()
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

                Log.d(TAG, "${inputData.size}")

                val inputDataArray = arrayOf(inputData, inputData)

                updater.retrain(inputData = inputDataArray)

                var result = classifier2.run(inputData)
                Log.d(TAG, "${result}")
            }
        }

        val workRequest = PeriodicWorkRequestBuilder<UpdateWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(15, TimeUnit.HOURS)
            .build()
        val workManager = WorkManager.getInstance(application)
        workManager.enqueueUniquePeriodicWork("UpdateWork", ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }

    public override fun onResume() {
        super.onResume()

        if (isSdkReady) {
            loadDevices()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (::updater.isInitialized) updater.finish()
        if (::classifier2.isInitialized) classifier2.finish()
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
        if(!isMyServiceRunning(FeatureService::class.java) && !isMyServiceRunning(TimerService::class.java)){
            Toast.makeText(applicationContext, "Starting Intervention...", Toast.LENGTH_SHORT).show()
            startService(FeatureService.putIntent(this, device))
            startService(TimerService.putIntent(this))
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

    private fun initUpdater() {
        updater = Updater(assets, Classifier.STRESS_CLASSIFIER)
        try {
            updater.init()
        } catch (exception: IOException) {
            Log.d(TAG, "IOException")
        }
    }

    private fun initClassifier2() {
        classifier2 = Classifier2(assets, Classifier.STRESS_CLASSIFIER)
        try {
            classifier2.init()
        } catch (exception: IOException) {
            Log.d(TAG, "IOException")
        }
    }
}