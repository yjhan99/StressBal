package com.garmin.android.apps.connectiq.sample.comm2.model

import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class Classifier(private val assetManager: AssetManager, private val modelName: String) {
    lateinit var interpreter: Interpreter

    private fun init() {
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

    companion object {
        const val DIGIT_CLASSIFIER = "model.tflite"
    }
}