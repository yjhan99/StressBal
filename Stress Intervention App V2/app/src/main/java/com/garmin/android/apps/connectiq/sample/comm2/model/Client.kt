package com.garmin.android.apps.connectiq.sample.comm2.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.ConditionVariable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.garmin.android.apps.connectiq.sample.comm2.activities.MainActivity
import com.garmin.android.apps.connectiq.sample.comm2.roomdb.AppDatabase
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.Buffer
import java.nio.ByteBuffer
import java.util.concurrent.ExecutionException

class Client (input_context: Context?) {
    private var tlModel: TransferLearningModelWrapper? = TransferLearningModelWrapper(input_context!!)
    private val lastLoss: MutableLiveData<Float> = MutableLiveData()
    private var context: Context? = input_context
    private val isTraining: ConditionVariable = ConditionVariable()
    private val TAG = "Client"
    private var local_epochs = 1

    fun Client(context: Context?) {
        tlModel = TransferLearningModelWrapper(context!!)
        this.context = context
    }

    fun getWeights(): Array<ByteBuffer> {
        return tlModel!!.parameters
    }

    fun fit(epochs: Int): Pair<Array<ByteBuffer>, Int> {
        local_epochs = epochs
        isTraining.close()
        tlModel!!.train(local_epochs)
        tlModel!!.enableTraining { epoch: Int, loss: Float ->
            setLastLoss(
                epoch,
                loss
            )
        }
        Log.e(TAG, "Training enabled. Local Epochs = " + local_epochs)
        isTraining.block()
        return Pair(getWeights(), tlModel!!.size_Training)
    }

    fun evaluate(weights: Array<ByteBuffer?>?): Pair<android.util.Pair<Float, Float>, Int> {
        tlModel!!.updateParameters(weights)
        tlModel!!.disableTraining()
        return Pair(tlModel!!.calculateTestStatistics(), tlModel!!.size_Testing)
    }

    fun inference(inputData: FloatArray): String {
        var result = tlModel!!.predict(inputData)
        return result.get(0).className
    }

    fun setLastLoss(epoch: Int, newLoss: Float) {
        if (epoch == local_epochs - 1) {
            Log.e(TAG, "Training finished after epoch = $epoch")
            lastLoss.postValue(newLoss)
            tlModel!!.disableTraining()
            isTraining.open()
        }
    }

    fun loadData(personalData: FloatArray, sampleClass: String) {
        try {
            /*var reader =
                BufferedReader(InputStreamReader(context!!.resources.assets.open("data/partition_train.txt")))
            //var reader =
            //    BufferedReader(InputStreamReader(context!!.assets.open("data/partition_train.txt")))
            var line: String
            var i = 0
            var iterator = reader.lineSequence().iterator()
            // while (reader.readLine().also { line = it } != null) {
            while (iterator.hasNext()) {
                line = iterator.next()
                i++
                Log.e(TAG, i.toString() + "th training data loaded")
                addSample("$line", true)
            }
            reader.close()
            i = 0
            reader =
                BufferedReader(InputStreamReader(context!!.assets.open("data/partition_test.txt")))
            iterator = reader.lineSequence().iterator()
            while (iterator.hasNext()) {
                line = iterator.next()
                i++
                Log.e(TAG, i.toString() + "th test data loaded")
                addSample("$line", false)
            }
            reader.close()
            */
            //addSample(personalData, sampleClass, true)
            try {
                //tlModel!!.addSample(personalData, sampleClass, true).get()
                tlModel!!.addSample(personalData, sampleClass, true)
                Log.d(TAG, tlModel.toString())
                Log.d(TAG, sampleClass.toString())
            } catch (e: ExecutionException) {
                throw RuntimeException("Failed to add sample to model")
            } catch (e: InterruptedException) {
                // no-op
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun addSample(personalData:FloatArray, sampleClass: String, isTraining: Boolean) {
        Log.d(TAG, tlModel.toString())
        Log.d(TAG, sampleClass.toString())
        Log.d(TAG, isTraining.toString())
        try {
            tlModel!!.addSample(personalData, sampleClass, isTraining).get()
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to add sample to model")
        } catch (e: InterruptedException) {
            // no-op
        }
    }

    @Throws(IOException::class)
    private fun addSampleOld(Path: String, isTraining: Boolean) {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap: Bitmap? =
            BitmapFactory.decodeStream(context!!.assets.open(Path), null, options)
        val sampleClass: String = get_class(Path)

        //val rgbImage: FloatArray = prepareImage(bitmap!!)
        var personalData = FloatArray(15)
        val recentData = AppDatabase.getInstance(context!!).userDAO().readRecentData()
        if(recentData == null) {
            Log.d(TAG,"There is no recent data")
        }
        else {
            Log.d(TAG, "input data start")
            personalData.plus(recentData.HRV!!.toFloat())
            personalData.plus(recentData.meanX!!.toFloat())
            personalData.plus(recentData.stdX!!.toFloat())
            personalData.plus(recentData.magX!!.toFloat())
            personalData.plus(recentData.meanY!!.toFloat())
            personalData.plus(recentData.stdY!!.toFloat())
            personalData.plus(recentData.magY!!.toFloat())
            personalData.plus(recentData.meanZ!!.toFloat())
            personalData.plus(recentData.stdZ!!.toFloat())
            personalData.plus(recentData.magZ!!.toFloat())
            personalData.plus(recentData.step!!.toFloat())
            var distance = if (recentData.distance == true) 1F else 0F
            personalData.plus(distance)
            var home = if (recentData.home == true) 1F else 0F
            personalData.plus(home)
            var work = if (recentData.work == true) 1F else 0F
            personalData.plus(work)
            personalData.plus(recentData.currentTime!!.toFloat())
            Log.d(TAG, "${personalData.size}")
        }

        // add to the list.
        Log.d(TAG, tlModel.toString())
        Log.d(TAG, sampleClass.toString())
        Log.d(TAG, isTraining.toString())
        try {
            tlModel!!.addSample(personalData, sampleClass, isTraining).get()
        } catch (e: ExecutionException) {
            throw RuntimeException("Failed to add sample to model")
        } catch (e: InterruptedException) {
            // no-op
        }
    }

    fun get_class(path: String): String {
        return path.split("/").toTypedArray()[2]
    }

    /*
    private fun prepareImage(bitmap: Bitmap): FloatArray {
        val modelImageSize = TransferLearningModelWrapper.IMAGE_SIZE
        val normalizedRgb = FloatArray(modelImageSize * modelImageSize * 3)
        var nextIdx = 0
        for (y in 0 until modelImageSize) {
            for (x in 0 until modelImageSize) {
                val rgb = bitmap.getPixel(x, y)
                val r = (rgb shr 16 and LOWER_BYTE_MASK) * (1 / 255.0f)
                val g = (rgb shr 8 and LOWER_BYTE_MASK) * (1 / 255.0f)
                val b = (rgb and LOWER_BYTE_MASK) * (1 / 255.0f)
                normalizedRgb[nextIdx++] = r
                normalizedRgb[nextIdx++] = g
                normalizedRgb[nextIdx++] = b
            }
        }
        return normalizedRgb
    }
    */
}