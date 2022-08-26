package com.garmin.android.apps.connectiq.sample.comm

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.garmin.android.apps.connectiq.sample.comm.roomdb.AppDatabase

class UpdateWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) { // If you want coroutines, CoroutineWorker()

    private val TAG= "UpdateWorker"
    private var DBhelper = AppDatabase.getInstance(context = context)

    override fun doWork(): Result {
        return try {
            Log.d(TAG,"success")

            val daybftimestamp = System.currentTimeMillis() - 24*60*60*1000 //1일전 timestamp
            val dbdata = DBhelper.labelDAO().readLabelData(daybftimestamp)
            dbdata.forEach { data ->
                val minus10time = data.currentTime - 10*60*1000
                val plus10time = data.currentTime + 10*60*1000
                DBhelper.userDAO().updateEMAResult(data.label, minus10time, plus10time)
            }
            Log.d(TAG, "label data updated")
            Result.success() // return statement

        } catch (e: Exception) {
            Log.d(TAG, "failure")
            Result.failure() // return statement
        }
    }
}
