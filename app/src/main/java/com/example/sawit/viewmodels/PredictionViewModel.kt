package com.example.sawit.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawit.ml.PredictionUtils
import com.example.sawit.models.PredictionHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseRef =
        FirebaseDatabase.getInstance("https://sawit-6876f-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("predictions")

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String get() = auth.currentUser?.uid ?: ""

    private val _predictionResult = MutableStateFlow<PredictionResult?>(null)
    val predictionResult = _predictionResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()

    sealed class Event {
        data class ShowError(val message: String) : Event()
        data class ShowMessage(val message: String) : Event()
        object PredictionSaved : Event()
    }

    data class PredictionResult(
        val predictedYield: Float,
        val conditionLabel: String? = null,
        val gapPercentage: Float? = null
    )

    fun clearPredictionResult() {
        _predictionResult.value = null
    }

    fun predictTotalYield(
        fieldId: String,
        fieldName: String,
        tmin: Float,
        tmax: Float,
        rainfall: Float,
        area: Float
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val yield = withContext(Dispatchers.IO) {
                    PredictionUtils.predictYield(getApplication(), tmin, tmax, rainfall, area)
                }

                val sanitizedYield = maxOf(0f, yield)

                val history = PredictionHistory(
                    fieldId = fieldId,
                    fieldName = fieldName,
                    predictionType = "Total Yield",
                    predictedYield = sanitizedYield,
                    tmin = tmin,
                    tmax = tmax,
                    rainfall = rainfall,
                    area = area,
                    date = Date(System.currentTimeMillis())
                )

                saveToDatabase(history)
                _predictionResult.value = PredictionResult(predictedYield = yield)

            } catch (e: Exception) {
                _eventChannel.send(Event.ShowError("ML Error: ${e.localizedMessage}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun predictPlantCondition(
        fieldId: String,
        fieldName: String,
        tmin: Float,
        tmax: Float,
        rainfall: Float,
        area: Float,
        actualYield: Float
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val predictedYield = withContext(Dispatchers.IO) {
                    PredictionUtils.predictYield(getApplication(), tmin, tmax, rainfall, area)
                }

                val sanitizedPredictedYield = maxOf(0f, predictedYield)

                val gap =
                    if (sanitizedPredictedYield != 0f) {
                        ((actualYield - sanitizedPredictedYield) / sanitizedPredictedYield) * 100
                    } else if (actualYield > 0f) {
                        100f
                    } else {
                        0f
                    }

                val label = when {
                    gap >= 15 -> "Good"
                    gap <= -15 -> "Bad"
                    else -> "Moderate"
                }

                val history = PredictionHistory(
                    fieldId = fieldId,
                    fieldName = fieldName,
                    predictionType = "Condition",
                    predictedYield = sanitizedPredictedYield,
                    actualYield = actualYield,
                    conditionLabel = label,
                    gapPercentage = gap,
                    tmin = tmin,
                    tmax = tmax,
                    rainfall = rainfall,
                    area = area,
                    date = Date(System.currentTimeMillis())
                )

                saveToDatabase(history)
                _predictionResult.value = PredictionResult(sanitizedPredictedYield, label, gap)

            } catch (e: Exception) {
                _eventChannel.send(Event.ShowError("Prediction Error: ${e.localizedMessage}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveToDatabase(history: PredictionHistory) {
        if (currentUserId.isEmpty()) {
            viewModelScope.launch { _eventChannel.send(Event.ShowError("User not logged in!")) }
            return
        }

        val newKey = databaseRef.push().key ?: return
        val historyWithId = history.copy(id = newKey, userId = currentUserId)

        databaseRef.child(newKey).setValue(historyWithId)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _eventChannel.send(Event.PredictionSaved)
                }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowError("Saving to database failed: ${e.localizedMessage}"))
                }
            }
    }
}