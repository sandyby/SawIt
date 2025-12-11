package com.example.sawit.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawit.models.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date

class ActivityViewModel : ViewModel() {
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities
    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    sealed class Event {
        data class ShowMessage(val message: String) : Event()
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""
    private val databaseRef =
        FirebaseDatabase.getInstance("https://sawit-6876f-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("activities")
    private lateinit var activityListener: ValueEventListener
    private var isListenerInitialized = false

//    init {
//        loadHardcodedActivities()
//    }

    fun listenForActivitiesUpdate() {
        if (isListenerInitialized || currentUserId.isEmpty()) {
            if (currentUserId.isEmpty()) {
                Log.e("ActivityViewModel", "Cannot start listening: User ID is missing!")
            }
            return
        }

        val activitiesByUser = databaseRef.orderByChild("userId").equalTo(currentUserId)

        activityListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activitiesList = mutableListOf<Activity>()
                for (activitySnapshot in snapshot.children) {
                    try {
                        val activity = activitySnapshot.getValue(Activity::class.java)
                        activity?.let { activitiesList.add(it) }
                    } catch (e: Exception) {
                        Log.e("ActivityViewModel", "Parsing error in listener: ${e.message}")
                    }
                }

                viewModelScope.launch {
                    _activities.value = activitiesList // Update StateFlow with live data
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ActivityViewModel", "Database listener cancelled: ${error.message}")
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowMessage("Failed to fetch activities: ${error.message}"))
                }
            }
        }

        activitiesByUser.addValueEventListener(activityListener)
        isListenerInitialized = true
        Log.d(
            "ActivityViewModel",
            "Firebase listener for user ${currentUserId}'s activities was added"
        )
    }

    override fun onCleared() {
        super.onCleared()
        if (isListenerInitialized) {
            val userSpecificQuery = databaseRef.orderByChild("userId").equalTo(currentUserId)
            userSpecificQuery.removeEventListener(activityListener)
            Log.d("ActivityViewModel", "Firebase listener for activities was removed.")
        }
    }

    fun createNewActivity(activity: Activity) {
        if (currentUserId.isEmpty()) {
            viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Error: Must be logged in to create activity.")) }
            return
        }

        _isLoading.value = true
        val newKey = databaseRef.push().key

        if (newKey != null) {
            val activityToSave = activity.copy(id = newKey, userId = currentUserId)
            databaseRef.child(newKey).setValue(activityToSave)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _eventChannel.send(Event.ShowMessage("Successfully created new activity!"))
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    viewModelScope.launch {
                        _eventChannel.send(Event.ShowMessage("Something went wrong while trying to create the activity!"))
                    }
                    _isLoading.value = false
                }
        } else {
            _isLoading.value = false
        }
    }

    fun updateActivity(activity: Activity) {
        if (currentUserId.isEmpty() || activity.userId != currentUserId) {
            viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Error: Cannot update activity you do not own.")) }
            return
        }

        _isLoading.value = true
        val activityId = activity.id

        if (activityId != null) {
            databaseRef.child(activityId).setValue(activity)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _eventChannel.send(Event.ShowMessage("Successfully updated the activity!"))
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    viewModelScope.launch {
                        _eventChannel.send(Event.ShowMessage("Something went wrong while trying to update the activity!"))
                    }
                    _isLoading.value = false
                }
        } else {
            _isLoading.value = false
        }
    }

    fun updateActivityStatus(id: String?, newStatus: String) {
        _activities.value = _activities.value.map { activity ->
            if (activity.id == id) activity.copy(status = newStatus) else activity
        }
    }

    fun deleteActivity(activityId: String?) {
        if (activityId == null || currentUserId.isEmpty()) return
        databaseRef.child(activityId).removeValue()
            .addOnSuccessListener {
                viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Activity deleted successfully!")) }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Failed to delete activity.")) }
            }
//        _activities.value = _activities.value.filter { it.id != id }
    }

    fun loadHardcodedActivities() {
        val dummyList = listOf(
            Activity(
                id = "1",
                fieldName = "Lahan 1",
                activityType = "Harvest",
                date = Date(),
                notes = "Pemanenan di blok A-3, fokus pada buah yang sudah matang.",
                status = "planned"
            ),
            Activity(
                id = "2",
                fieldName = "Lahan Manjur Sukses",
                activityType = "Fertilizing",
                date = Date(),
                notes = "Pemupukan dengan NPK seimbang di seluruh area.",
                status = "planned"
            ),
            Activity(
                id = "3",
                fieldName = "Lahan 1",
                activityType = "Pruning",
                date = Date(System.currentTimeMillis() - 86400000 * 2), // 2 hari yang lalu
                notes = "Pelepasan pelepah kering telah selesai.",
                status = "completed"
            ),
            Activity(
                id = "4",
                fieldName = "Kebun Belakang",
                activityType = "Scouting",
                date = Date(System.currentTimeMillis() - 86400000 * 5), // 5 hari yang lalu
                notes = "Pengecekan hama dan penyakit, tidak ditemukan anomali.",
                status = "completed"
            )
        )
        _activities.value = dummyList
    }
}

