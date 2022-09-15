package com.garmin.android.apps.connectiq.sample.comm2.model

import android.content.Context
import android.gesture.Prediction
import android.os.ConditionVariable
import android.util.Pair
import org.tensorflow.lite.examples.transfer.api.AssetModelLoader
import org.tensorflow.lite.examples.transfer.api.ModelLoader
import org.tensorflow.lite.examples.transfer.api.TransferLearningModel
import org.tensorflow.lite.examples.transfer.api.TransferLearningModel.LossConsumer
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

class TransferLearningModelWrapper internal constructor(context: Context) :
    Closeable {
    private val model: TransferLearningModel
    private val shouldTrain = ConditionVariable()

    @Volatile
    private var lossConsumer: LossConsumer? = null
    private val context: Context
    fun train(epochs: Int) {
        Thread {
            shouldTrain.block()
            try {
                model.train(epochs, lossConsumer).get()
            } catch (e: ExecutionException) {
                throw RuntimeException(
                    "Exception occurred during model training",
                    e.cause
                )
            } catch (e: InterruptedException) {
                // no-op
            }
        }.start()
    }

    // This method is thread-safe.
    fun addSample(image: FloatArray?, className: String?, isTraining: Boolean?): Future<Void> {
        return model.addSample(image, className, isTraining)
    }

    fun calculateTestStatistics(): Pair<Float, Float> {
        return model.testStatistics
    }

    // This method is thread-safe, but blocking.
    fun predict(image: FloatArray?): Array<TransferLearningModel.Prediction> {
        return model.predict(image)
    }

    val trainBatchSize: Int
        get() = model.trainBatchSize

    /**
     * Start training the model continuously until [disableTraining][.disableTraining] is
     * called.
     *
     * @param lossConsumer callback that the loss values will be passed to.
     */
    fun enableTraining(lossConsumer: LossConsumer?) {
        this.lossConsumer = lossConsumer
        shouldTrain.open()
    }

    fun createChannelInstance(file: File?, isOutput: Boolean): FileChannel? {
        var fc: FileChannel? = null
        try {
            if (isOutput) {
                fc = FileOutputStream(file).channel
            } else {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return fc
    }

    val size_Training: Int
        get() = model.size_Training
    val size_Testing: Int
        get() = model.size_Testing
    val parameters: Array<ByteBuffer>
        get() = model.parameters

    fun updateParameters(newParams: Array<ByteBuffer?>?) {
        model.updateParameters(newParams)
    }

    /**
     * Stops training the model.
     */
    fun disableTraining() {
        shouldTrain.close()
    }

    /** Frees all model resources and shuts down all background threads.  */
    override fun close() {
        model.close()
    }

    companion object {
        /**
         * CIFAR10 image size. This cannot be changed as the TFLite model's input layer expects
         * a 32x32x3 input.
         */
        const val IMAGE_SIZE = 15
    }

    init {
        model = TransferLearningModel(
            AssetModelLoader(context, "model"),
            Arrays.asList(
                "0","1"
            )
        )
        this.context = context
    }
}