package com.garmin.android.apps.connectiq.sample.comm2.model

import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class Classifier(private val assetManager: AssetManager, private val modelName: String) {
    private val TAG = "Classifier"

    lateinit var interpreter: Interpreter

    private var modelInputChannel = 0
    private var modelInput = 0
    //private var modelInputHeight = 0

    private var modelOutputClasses = 0

    fun init() {
        val model = loadModelFile()
        model.order(ByteOrder.nativeOrder())
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(): ByteBuffer {
        val assetFileDescriptor = assetManager.openFd(modelName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun initModelShape() {
        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        modelInputChannel = inputShape[0]
        modelInput = inputShape[1]
        //modelInputHeight = inputShape[2]

        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()
        modelOutputClasses = outputShape[1]
    }

    fun classify(data: InputData): Int {
        val result = Array(1) { FloatArray(modelOutputClasses) { 0f } }
        interpreter.run(data, result)
        return argmax(result[0])
    }

    private fun argmax(array: FloatArray): Int {
        var maxIndex = 0
        var maxValue = 0f
        array.forEachIndexed { index, value ->
            if (value > maxValue) {
                maxIndex = index
                maxValue = value
            }
        }
        Log.d(TAG, "Result: ${maxIndex} Prob: ${maxValue}")
        return maxIndex
    }

    fun finish() {
        if (::interpreter.isInitialized) interpreter.close()
    }

    companion object {
        const val STRESS_CLASSIFIER = "model.tflite"
    }
}