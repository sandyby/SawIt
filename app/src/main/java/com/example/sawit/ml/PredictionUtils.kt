package com.example.sawit.ml

import android.content.Context
import android.util.Log // Import Android Log
import ai.onnxruntime.*
import java.nio.FloatBuffer

object PredictionUtils {
    private const val TAG = "SAWIT_ML_DEBUG" // Tag khusus untuk filtering
    private const val INPUT_NAME = "float_input"
    private val INPUT_SHAPE = longArrayOf(1, 4)

    fun predictConditionClassifier(
        context: Context,
        tmin: Float,
        tmax: Float,
        rainfall: Float,
        area: Float
    ): Int {
        Log.d(TAG, "--------------------------------------------------")
        Log.d(TAG, "START: Condition Classifier Inference")
        Log.d(TAG, "Loading Model: classifier_condition.onnx")

        val session = ORTSessionHelper.loadModel(context, "classifier_condition.onnx")

        val inputArray = floatArrayOf(tmin, tmax, rainfall, area)
        Log.d(TAG, "Input Values: Tmin=$tmin, Tmax=$tmax, Rain=$rainfall, Area=$area")

        // Logika pembuatan Tensor
        val tensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), FloatBuffer.wrap(inputArray), INPUT_SHAPE)
        Log.d(TAG, "Tensor Created: Shape=${INPUT_SHAPE.joinToString("x")}")

        // Proses Menjalankan Model AI
        Log.d(TAG, "Executing session.run() - Processing through ONNX neural network...")
        val result = session.run(mapOf(INPUT_NAME to tensor))

        val outputIndex = (result.get(0).value as LongArray)[0].toInt()
        Log.d(TAG, "SUCCESS: Model Predicted Class Index = $outputIndex")
        Log.d(TAG, "--------------------------------------------------")

        result.close()
        return outputIndex
    }

    // Fungsi Prediksi Yield (Hasil Panen)
    fun predictYield(
        context: Context,
        tmin: Float,
        tmax: Float,
        rainfall: Float,
        area: Float
    ): Float {
        Log.i(TAG, "==================================================")
        Log.i(TAG, "START: Yield Regressor Inference")
        Log.i(TAG, "Model Path: assets/regressor_yield.onnx")

        val session = ORTSessionHelper.loadModel(context, "regressor_yield.onnx")

        val inputArray = floatArrayOf(tmin, tmax, rainfall, area)
        Log.i(TAG, "Input Array prepared for Model: [${inputArray.joinToString(", ")}]")

        // Membuat tensor dari FloatBuffer
        val tensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), FloatBuffer.wrap(inputArray), INPUT_SHAPE)

        // Bukti Model AI Berjalan
        Log.i(TAG, "Inference Engine: Predicting via ONNX Runtime...")
        val result = session.run(mapOf(INPUT_NAME to tensor))

        // Output dari regressor model adalah FloatArray of [1, 1]
        val output = (result.get(0).value as Array<FloatArray>)[0][0]

        Log.i(TAG, "RAW MODEL RESULT: $output")
        Log.i(TAG, "FINAL PREDICTION: $output kg/Ha")
        Log.i(TAG, "==================================================")

        result.close()
        return output
    }
}