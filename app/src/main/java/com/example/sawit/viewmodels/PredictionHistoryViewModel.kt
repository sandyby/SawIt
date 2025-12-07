// File: com/example/sawit/viewmodels/PredictionHistoryViewModel.kt
package com.example.sawit.viewmodels

import androidx.lifecycle.ViewModel
import com.example.sawit.data.roaming.PredictionHistoryManager // <--- Menggunakan Manager Baru
import com.example.sawit.models.Prediction
import kotlinx.coroutines.flow.Flow

class PredictionHistoryViewModel : ViewModel() { // <--- Penamaan Baru
    private val manager = PredictionHistoryManager()

    // Flow untuk riwayat real-time
    val allHistory: Flow<List<Prediction>> = manager.getAllHistory()
}