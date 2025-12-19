package com.example.sawit.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawit.models.PredictionHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PredictionHistoryViewModel : ViewModel() {
    private val _predictionHistoriesData = MutableStateFlow<List<PredictionHistory>>(emptyList())
    val predictionHistoriesData: StateFlow<List<PredictionHistory>> =
        _predictionHistoriesData.asStateFlow()

    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val databaseRef =
        FirebaseDatabase.getInstance("https://sawit-6876f-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("predictions")

    private lateinit var historyListener: ValueEventListener
    private var isListenerInitialized: Boolean = false

    sealed class Event {
        data class ShowMessage(val message: String) : Event()
        object FinishActivity : Event()
    }

    init {
        listenForHistoryUpdates()
    }

    fun listenForHistoryUpdates() {
        if (isListenerInitialized || currentUserId.isEmpty()) return

        val historyByUserQuery = databaseRef.orderByChild("userId").equalTo(currentUserId)

        historyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = mutableListOf<PredictionHistory>()
                for (historySnapshot in snapshot.children) {
                    try {
                        val item = historySnapshot.getValue(PredictionHistory::class.java)
                        item?.let { historyList.add(it) }
                    } catch (e: Exception) {
                        Log.e("PredictionVM", "Parsing error: ${e.message}")
                    }
                }
                _predictionHistoriesData.value = historyList.sortedByDescending { it.date }
            }

            override fun onCancelled(error: DatabaseError) {
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowMessage("Failed to fetch history: ${error.message}"))
                }
            }
        }
        historyByUserQuery.addValueEventListener(historyListener)
        isListenerInitialized = true
    }

    fun savePrediction(prediction: PredictionHistory) {
        if (currentUserId.isEmpty()) return

        _isLoading.value = true
        val newKey = databaseRef.push().key

        if (newKey != null) {
            val predictionToSave = prediction.copy(id = newKey, userId = currentUserId)
            databaseRef.child(newKey).setValue(predictionToSave)
                .addOnSuccessListener {
                    _isLoading.value = false
                    viewModelScope.launch {
                        _eventChannel.send(Event.ShowMessage("Prediction saved successfully!"))
                    }
                }
                .addOnFailureListener {
                    _isLoading.value = false
                }
        }
    }

    fun deleteHistory(id: String) {
        _isLoading.value = true
        databaseRef.child(id).removeValue()
            .addOnSuccessListener {
                _isLoading.value = false
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowMessage("History deleted"))
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        if (isListenerInitialized) {
            databaseRef.orderByChild("userId").equalTo(currentUserId).removeEventListener(historyListener)
        }
    }
}