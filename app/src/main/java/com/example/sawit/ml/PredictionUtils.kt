package com.example.sawit.ml

import android.content.Context
import ai.onnxruntime.*
import java.nio.FloatBuffer

object PredictionUtils {
    private const val INPUT_NAME = "float_input"
    private val INPUT_SHAPE = longArrayOf(1, 4)

    fun predictConditionClassifier(
        context: Context,
        tmin: Float,
        tmax: Float,
        rainfall: Float,
        area: Float
    ): Int {

        val session = ORTSessionHelper.loadModel(context, "classifier_condition.onnx")

        val inputArray = floatArrayOf(tmin, tmax, rainfall, area)

        val tensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), FloatBuffer.wrap(inputArray), INPUT_SHAPE)

        val result = session.run(mapOf(INPUT_NAME to tensor))
        val outputIndex = (result.get(0).value as LongArray)[0].toInt()

        result.close()
        return outputIndex
    }

    fun predictYield(
        context: Context,
        tmin: Float,
        tmax: Float,
        rainfall: Float,
        area: Float
    ): Float {
        val session = ORTSessionHelper.loadModel(context, "regressor_yield.onnx")
        val inputArray = floatArrayOf(tmin, tmax, rainfall, area)
        val tensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), FloatBuffer.wrap(inputArray), INPUT_SHAPE)
        val result = session.run(mapOf(INPUT_NAME to tensor))
        val output = (result.get(0).value as Array<FloatArray>)[0][0]

        result.close()
        return output
    }
}