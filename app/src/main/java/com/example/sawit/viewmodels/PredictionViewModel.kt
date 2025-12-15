// File: com/example/sawit/viewmodels/PredictionViewModel.kt
package com.example.sawit.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawit.ml.PredictionUtils // Asumsi Anda memiliki kelas ini
import com.example.sawit.models.Prediction
import com.example.sawit.data.roaming.PredictionHistoryManager // <--- Menggunakan Manager Baru

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val historyManager = PredictionHistoryManager() // <--- Menggunakan Manager Baru

    private val context: Context
        get() = getApplication<Application>().applicationContext


    fun predictAndSaveTotalPanen(
        fieldName: String, tmin: Float, tmax: Float, rainfall: Float, area: Float,
        onSuccess: (predictedYield: Float) -> Unit, onError: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val predictedYield = withContext(Dispatchers.IO) {
                    PredictionUtils.predictYield(context, tmin, tmax, rainfall, area)
                }

                val historyEntry = Prediction(
                    fieldName = fieldName,
                    predictionType = "Total Panen",
                    predictedYield = predictedYield,
                    tmin = tmin, tmax = tmax, rainfall = rainfall, area = area
                )
                historyManager.savePrediction(historyEntry)

                onSuccess(predictedYield)

            } catch (e: Exception) {
                onError("Gagal prediksi Total Panen: ${e.message}")
            }
        }
    }


    fun predictAndSaveKondisiTanaman(
        fieldName: String, tmin: Float, tmax: Float, rainfall: Float, area: Float, actualYield: Float,
        onSuccess: (conditionLabel: String, predictedYield: Float, gapPercentage: Float) -> Unit,
        onError: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val predictedYield = withContext(Dispatchers.IO) {
                    PredictionUtils.predictYield(context, tmin, tmax, rainfall, area)
                }

                val gapPercentage = ((actualYield - predictedYield) / (predictedYield.takeIf { it != 0f } ?: 1e-6f)) * 100
                val conditionLabel = when {
                    gapPercentage >= 15 -> "Good"
                    gapPercentage <= -15 -> "Bad"
                    else -> "Enough"
                }

                val historyEntry = Prediction(
                    fieldName = fieldName,
                    predictionType = "Kondisi",
                    predictedYield = predictedYield,
                    actualYield = actualYield,
                    conditionLabel = conditionLabel,
                    gapPercentage = gapPercentage,
                    tmin = tmin, tmax = tmax, rainfall = rainfall, area = area
                )
                historyManager.savePrediction(historyEntry)

                onSuccess(conditionLabel, predictedYield, gapPercentage)

            } catch (e: Exception) {
                onError("Gagal prediksi Kondisi Tanaman: ${e.message}")
            }
        }
    }
}