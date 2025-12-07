package com.example.sawit.ml

import android.content.Context
import ai.onnxruntime.* // Mengaktifkan ONNX Runtime
import java.nio.FloatBuffer

object PredictionUtils {

    // Nama tensor input dari model ONNX yang kita buat sebelumnya
    private const val INPUT_NAME = "float_input"
    private val INPUT_SHAPE = longArrayOf(1, 4) // [batch_size=1, num_features=4]

    // Fungsi Prediksi Kondisi (menggunakan model klasifikasi jika diperlukan)
    // Walaupun kita menggunakan metode Gap Calculation, fungsi ini tetap berguna
    // jika Anda ingin memprediksi kondisi secara langsung tanpa input hasil panen aktual.
    fun predictConditionClassifier(
        context: Context,
        tmin: Float,
        tmax: Float,
        rainfall: Float,
        area: Float
    ): Int { // Mengembalikan indeks label: 0 (Buruk), 1 (Cukup), 2 (Baik)

        val session = ORTSessionHelper.loadModel(context, "classifier_condition.onnx")

        val inputArray = floatArrayOf(tmin, tmax, rainfall, area)

        // Membuat tensor dari FloatBuffer
        val tensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), FloatBuffer.wrap(inputArray), INPUT_SHAPE)

        val result = session.run(mapOf(INPUT_NAME to tensor))
        // Output adalah LongArray dari label (indeks)
        val outputIndex = (result.get(0).value as LongArray)[0].toInt()

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

        val session = ORTSessionHelper.loadModel(context, "regressor_yield.onnx")

        val inputArray = floatArrayOf(tmin, tmax, rainfall, area)

        // Membuat tensor dari FloatBuffer
        val tensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), FloatBuffer.wrap(inputArray), INPUT_SHAPE)

        val result = session.run(mapOf(INPUT_NAME to tensor))
        // Output dari regressor model adalah FloatArray of [1, 1]
        val output = (result.get(0).value as Array<FloatArray>)[0][0]

        result.close()
        return output
    }
}