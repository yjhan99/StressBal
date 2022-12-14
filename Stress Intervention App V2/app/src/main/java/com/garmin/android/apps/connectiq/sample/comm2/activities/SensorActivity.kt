package com.garmin.android.apps.connectiq.sample.comm2.activities

import android.Manifest
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.garmin.android.apps.connectiq.sample.comm2.SensorFactory
import com.garmin.android.apps.connectiq.sample.comm2.adapter.SensorDatasAdapter
import com.garmin.android.apps.connectiq.sample.comm2.Service.LocationService
import com.garmin.android.apps.connectiq.sample.comm2.Service.ScreenService
import java.util.*
import com.garmin.android.apps.connectiq.sample.comm2.R


private const val TAG = "SensorActivity"

class SensorActivity : AppCompatActivity() {
    private val REQUEST_CODE_LOCATION_PERMISSION = 1

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        toolbar = findViewById(R.id.sensor_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        buildSensorDataList()
    }

    private fun buildSensorDataList(){
        val adapter = SensorDatasAdapter { onItemClick(it) }
        adapter.submitList(SensorFactory.getSensorDatas(this@SensorActivity))
        findViewById<RecyclerView>(android.R.id.list).apply {
            layoutManager = GridLayoutManager(this@SensorActivity, 2)
            this.adapter = adapter
        }
    }

    private fun onItemClick(datas: Any) {
        //TODO: ??? ????????? ?????? ??? ????????? ???
        Log.d(TAG, datas.toString())

        //location service
        if (datas.toString().equals(getString(R.string.start_location_update))) {
            Log.d(TAG, "location service ??????")
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@SensorActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            } else {
                if (isMyServiceRunning(LocationService::class.java)) {
                    Log.e(TAG, "Location Service is already running")
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Start Location Service..",
                        Toast.LENGTH_SHORT
                    ).show()
                    startService(Intent(this, LocationService::class.java))
                }
            }
        } else if (datas.toString().equals(getString(R.string.stop_location_updates))) {
            if (isMyServiceRunning(LocationService::class.java)) {
                Log.d(TAG, "location service ??????")
                Toast.makeText(applicationContext, "Stop Location Service", Toast.LENGTH_SHORT)
                    .show()
                stopService(Intent(this, LocationService::class.java))
            } else {
                Log.e(TAG, "Location Service is not running")
            }
        }


        // Phone usage service
        else if (datas.toString().equals("Start Phone Usage Updates")) {
            if (isMyServiceRunning(ScreenService::class.java)){
                Log.e(TAG, "Screen Service is already running")
            } else {
                Toast.makeText(applicationContext, "Start Phone Usage Updates", Toast.LENGTH_SHORT).show()
                startService(Intent(this, ScreenService::class.java))
            }
        } else if(datas.toString().equals("Stop Phone Usage Updates")){
            if (isMyServiceRunning(ScreenService::class.java)){
                Log.d(TAG, "Screen Service ??????")
                Toast.makeText(applicationContext, "Stop Phone Usage Updates", Toast.LENGTH_SHORT).show()
                stopService(Intent(this, ScreenService::class.java))
            } else {
                Log.e(TAG, "Screen Service is not running")
            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkForPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == MODE_ALLOWED
    }
    
}