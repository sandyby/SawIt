package com.example.sawit.viewmodels

import androidx.lifecycle.ViewModel
import com.example.sawit.data.roaming.PredictionHistoryManager
import com.example.sawit.models.Prediction
import kotlinx.coroutines.flow.Flow

class PredictionHistoryViewModel : ViewModel() {
    private val manager = PredictionHistoryManager()

    val allHistory: Flow<List<Prediction>> = manager.getAllHistory()
}