package com.garmin.android.apps.connectiq.sample.comm2

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.garmin.android.apps.connectiq.sample.comm2.model.Classifier
import com.garmin.android.apps.connectiq.sample.comm2.model.Client
import com.garmin.android.apps.connectiq.sample.comm2.roomdb.AppDatabase

class UpdateWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) { // If you want coroutines, CoroutineWorker()

    private val TAG= "UpdateWorker"
    private var DBhelper = AppDatabase.getInstance(context = context)

    private lateinit var client: Client

    override fun doWork(): Result {
        return try {
            Log.d(TAG,"success")

            client = Client(applicationContext)

            val daybftimestamp = System.currentTimeMillis() - 24*60*60*1000 //1일전 timestamp
            val labeldata = DBhelper.labelDAO().readLabelData(daybftimestamp)
            var personalData = FloatArray(14)

            labeldata.forEach { data ->
                val minus10time = data.currentTime - 10*60*1000
                val plus10time = data.currentTime + 10*60*1000
                DBhelper.userDAO().updateEMAResult(data.label, minus10time, plus10time)
            }
            val userdata = DBhelper.userDAO().readData(daybftimestamp)
            userdata.forEach { data ->
                if(data.label != 2) {
                    personalData.plus(data.HRV!!.toFloat())
                    personalData.plus(data.meanX!!.toFloat())
                    personalData.plus(data.stdX!!.toFloat())
                    personalData.plus(data.magX!!.toFloat())
                    personalData.plus(data.meanY!!.toFloat())
                    personalData.plus(data.stdY!!.toFloat())
                    personalData.plus(data.magY!!.toFloat())
                    personalData.plus(data.meanZ!!.toFloat())
                    personalData.plus(data.stdZ!!.toFloat())
                    personalData.plus(data.magZ!!.toFloat())
                    personalData.plus(data.step!!.toFloat())
                    var distance = if (data.distance == true) 1F else 0F
                    personalData.plus(distance)
                    var home = if (data.home == true) 1F else 0F
                    personalData.plus(home)
                    var work = if (data.work == true) 1F else 0F
                    personalData.plus(work)
                    client.loadData(personalData, data.label.toString())
                }
            }

            Result.success() // return statement

        } catch (e: Exception) {
            Log.d(TAG, "failure")
            Result.failure() // return statement
        }
    }


}
