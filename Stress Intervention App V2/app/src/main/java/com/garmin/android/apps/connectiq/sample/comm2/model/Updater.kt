package com.garmin.android.apps.connectiq.sample.comm2.model

import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.channels.FileChannel

class Updater(private val assetManager: AssetManager, private val modelName: String) {
    private val TAG = "Updater"

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

    fun retrain(inputData: Array<FloatArray>) {
        val numEpochs = 100
        val batchSize = 10
        val inputSize = 15
        val numTraining = inputData.size
        val numBatches = numTraining / batchSize

        val trainDataBatches: List<FloatBuffer> = ArrayList(numBatches)
        val trainLabelBatches: List<FloatBuffer> = ArrayList(numBatches)

        for (i in 0 until numBatches) {
            val trainData =
                FloatBuffer.allocate(batchSize * inputSize)
            val trainLabels =
                FloatBuffer.allocate(batchSize * 2)

            // Fill the data values...
            trainDataBatches.plus(trainData.rewind())
            trainLabelBatches.plus(trainLabels.rewind())
        }

        // Run training for a few steps.
        val losses = FloatArray(numEpochs)
        for (epoch in 0 until numEpochs) {
            for (batchIdx in 0 until numBatches) {
                val inputs: MutableMap<String, Any> = HashMap()
                inputs["x"] = trainDataBatches.get(batchIdx)
                inputs["y"] = trainLabelBatches[batchIdx]
                val outputs: MutableMap<String, Any> = HashMap()
                val loss = FloatBuffer.allocate(1)
                outputs["loss"] = loss
                interpreter.runSignature(inputs, outputs, "train")

                // Record the last loss.
                if (batchIdx == numBatches - 1) losses[epoch] = loss[0]
            }
        }

        // Export the trained weights as a checkpoint file.
        val outputFile = File("/data/data/com.garmin.android.apps.connectiq.sample.comm2", "checkpoint.ckpt")
        val inputs: MutableMap<String, Any> = HashMap()
        inputs["checkpoint_path"] = outputFile.getAbsolutePath()
        val outputs: Map<String, Any> = HashMap()
        interpreter.runSignature(inputs, outputs, "save")
    }

    fun finish() {
        if (::interpreter.isInitialized) interpreter.close()
    }

    companion object {
        const val STRESS_CLASSIFIER = "model.tflite"
    }
}