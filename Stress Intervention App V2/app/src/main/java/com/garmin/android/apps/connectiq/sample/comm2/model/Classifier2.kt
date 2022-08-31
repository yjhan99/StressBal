package com.garmin.android.apps.connectiq.sample.comm2.model

import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.channels.FileChannel

class Classifier2(private val assetManager: AssetManager, private val modelName: String) {
    private val TAG = "Classifier2"

    lateinit var interpreter: Interpreter

    private var modelInputChannel = 0
    private var modelInput = 0

    private var modelOutputClasses = 0

    fun init() {
        val model = loadModelFile()
        model.order(ByteOrder.nativeOrder())
        interpreter = Interpreter(model)
        initModelShape()
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

    fun run(testData: FloatArray) {
        // Load the trained weights from the checkpoint file.
        val outputFile: File = File("/data/data/com.garmin.android.apps.connectiq.sample.comm2", "checkpoint.ckpt")
        val inputs1: MutableMap<String, Any> = HashMap()
        inputs1["checkpoint_path"] = outputFile.absolutePath
        val outputs1: Map<String, Any> = HashMap()
        interpreter.runSignature(inputs1, outputs1, "restore")

        val NUM_TESTS = 2
        val testImages: FloatBuffer =
            FloatBuffer.allocate(NUM_TESTS * 15)
        val output: FloatBuffer =
            FloatBuffer.allocate(NUM_TESTS * 2)

        // Run the inference.
        val inputs2: MutableMap<String, Any> = HashMap()
        inputs2["x"] = testImages.rewind()
        val outputs2: MutableMap<String, Any> = HashMap()
        outputs2["output"] = output
        interpreter.runSignature(inputs2, outputs2, "infer")
        output.rewind()

        // Process the result to get the final category values.
        val testLabels = IntArray(NUM_TESTS)
        for (i in 0 until NUM_TESTS) {
            var index = 0
            for (j in 1..2) {
                if (output[i * 3 + index] < output[i * 3 + j]) index = testLabels[j]
            }
            testLabels[i] = index
        }
    }

    fun finish() {
        if (::interpreter.isInitialized) interpreter.close()
    }

    companion object {
        const val STRESS_CLASSIFIER = "model.tflite"
    }
}