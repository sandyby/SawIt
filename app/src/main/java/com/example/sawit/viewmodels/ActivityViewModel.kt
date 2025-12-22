package com.example.sawit.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.WorkManager
import com.example.sawit.models.Activity
import com.example.sawit.workers.ActivityReminderWorker
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

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities
    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()
    private val workManager = WorkManager.getInstance(application)
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

    init {
        if (currentUserId.isNotEmpty()) {
            listenForActivitiesUpdate()
        }
    }

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
                    _activities.value = activitiesList
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

    fun updateActivitiesFieldName(fieldId: String, newName: String) {
        val query = databaseRef.orderByChild("fieldId").equalTo(fieldId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = mutableMapOf<String, Any?>()
                for (activitySnapshot in snapshot.children) {
                    updates["/${activitySnapshot.key}/fieldName"] = newName
                }
                databaseRef.updateChildren(updates).addOnSuccessListener {
                    Log.d("ActivityViewModel", "Successfully synced field name for ${updates.size} activities")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ActivityViewModel", "Failed sync new field names: ${error.message}", )}
        })
    }

    override fun onCleared() {
        super.onCleared()
        if (isListenerInitialized) {
            val userSpecificQuery = databaseRef.orderByChild("userId").equalTo(currentUserId)
            userSpecificQuery.removeEventListener(activityListener)
            Log.d("ActivityViewModel", "Firebase listener for activities was removed.")
        }
    }

    fun scheduleNotification(activity: Activity) {
        val activityId = activity.id ?: return

        Log.d("NotificationDemo", "")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val demoDelay = 10000L
        val demoData = androidx.work.workDataOf(
            "title" to "Upcoming: ${activity.activityType}",
            "message" to "Reminder: Prepare for tomorrow's activity in ${activity.fieldName}.",
            "activityId" to activityId
        )

        val demoRequest = androidx.work.OneTimeWorkRequestBuilder<ActivityReminderWorker>()
            .setConstraints(constraints)
            .setInitialDelay(demoDelay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag("demo_$activityId")
            .setInputData(demoData)
            .build()

        workManager.enqueueUniqueWork(
            "demo_$activityId",
            androidx.work.ExistingWorkPolicy.REPLACE,
            demoRequest
        )

        val reminderTimeMillis = activity.date.time - (24 * 60 * 60 * 1000)
        val reminderDelay = reminderTimeMillis - System.currentTimeMillis()

        if (reminderDelay > 0) {
            val reminderData = androidx.work.workDataOf(
                "title" to "Upcoming: ${activity.activityType}",
                "message" to "Reminder: Prepare for tomorrow's activity in ${activity.fieldName}.",
                "activityId" to activityId
            )

            val reminderRequest = androidx.work.OneTimeWorkRequestBuilder<ActivityReminderWorker>()
                .setConstraints(constraints)
                .setInitialDelay(reminderDelay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .addTag("reminder_$activityId")
                .setInputData(reminderData)
                .build()

            workManager.enqueueUniqueWork(
                "reminder_$activityId",
                androidx.work.ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
        }

        Log.d(
            "NotificationDemo",
            "Demo scheduled (10s) and Real reminder scheduled (${reminderDelay / 1000}s)"
        )
    }

    private fun createNotificationForActivity(activity: Activity) {
        val userId = auth.currentUser?.uid ?: return
        val notificationRef = FirebaseDatabase.getInstance()
            .getReference("notifications")
            .child(userId)

        val notificationId = notificationRef.push().key ?: return

        val notificationData = mapOf(
            "id" to notificationId,
            "title" to "New Task Scheduled",
            "message" to "A new ${activity.activityType} has been added to ${activity.fieldName}.",
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false,
            "activityId" to activity.id
        )

        notificationRef.child(notificationId).setValue(notificationData)
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
                    createNotificationForActivity(activityToSave)
                    scheduleNotification(activityToSave)
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
                    scheduleNotification(activity)
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

    fun updateActivityStatus(activityId: String, newStatus: String) {
        databaseRef.child(activityId).child("status").setValue(newStatus)
            .addOnSuccessListener {
                if (newStatus.equals("completed", ignoreCase = true)) {
                    workManager.cancelUniqueWork("reminder_$activityId")
                    workManager.cancelUniqueWork("demo_$activityId")
                }
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowMessage("Status updated to $newStatus"))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Failed to update status")) }
            }
    }

    fun deleteActivity(activityId: String?) {
        if (activityId == null || currentUserId.isEmpty()) return
        databaseRef.child(activityId).removeValue()
            .addOnSuccessListener {
                workManager.cancelUniqueWork("demo_$activityId")
                workManager.cancelUniqueWork("reminder_$activityId")
                viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Activity deleted successfully!")) }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Failed to delete activity.")) }
            }
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
                date = Date(System.currentTimeMillis() - 86400000 * 2),
                notes = "Pelepasan pelepah kering telah selesai.",
                status = "completed"
            ),
            Activity(
                id = "4",
                fieldName = "Kebun Belakang",
                activityType = "Scouting",
                date = Date(System.currentTimeMillis() - 86400000 * 5),
                notes = "Pengecekan hama dan penyakit, tidak ditemukan anomali.",
                status = "completed"
            )
        )
        _activities.value = dummyList
    }
}

