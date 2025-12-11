package com.example.sawit.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawit.models.Field
import com.example.sawit.models.FieldLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

class FieldViewModel : ViewModel() {
    private val _fieldsData = MutableStateFlow<List<Field>>(emptyList())
    val fieldsData: StateFlow<List<Field>> = _fieldsData.asStateFlow()
    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    sealed class Event {
        data class ShowMessage(val message: String) : Event()
        object FinishActivity : Event()
    }

    private val databaseRef =
        FirebaseDatabase.getInstance("https://sawit-6876f-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("fields")

    private lateinit var fieldListener: ValueEventListener
    private var isListenerInitialized: Boolean = false

    init {
//        populateData()
        listenForFieldsUpdates()
    }

    fun listenForFieldsUpdates() {
        if (isListenerInitialized || currentUserId.isEmpty()) {
            if (currentUserId.isEmpty()) {
                Log.e("FieldViewModel", "User ID is missing!")
            }
            return
        }

        val fieldsByUserQuery = databaseRef.orderByChild("userId").equalTo(currentUserId)

        fieldListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fieldsList = mutableListOf<Field>()

                for (fieldSnapshot in snapshot.children) {
                    try {
                        val field = fieldSnapshot.getValue(Field::class.java)
                        field?.let { fieldsList.add(it) }
                    } catch (e: Exception) {
                        Log.e("FieldViewModel", "Parsing error in listener: ${e.message}")
                    }
                }

                viewModelScope.launch {
                    _fieldsData.value = fieldsList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FieldViewModel", "Database listener cancelled: ${error.message}")
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowMessage("Failed to fetch fields: ${error.message}"))
                }
            }
        }
        fieldsByUserQuery.addValueEventListener(fieldListener)
        isListenerInitialized = true
//        databaseRef.addValueEventListener(fieldListener)
        Log.d("FieldViewModel", "Firebase listener for fields was added")
    }

    override fun onCleared() {
        super.onCleared()
//        databaseRef.removeEventListener(fieldListener)
        if (isListenerInitialized) {
            val fieldsByUserQuery = databaseRef.orderByChild("userId").equalTo(currentUserId)
            fieldsByUserQuery.removeEventListener(fieldListener)
            Log.d("FieldViewModel", "Firebase listener for fields was removed.")
        }
    }

    fun createNewField(field: Field) {
        if (currentUserId.isEmpty()) {
            viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Please log in first to create field!")) }
            return
        }

        _isLoading.value = true
        val newKey = databaseRef.push().key

        if (newKey != null) {
            val fieldToSave = field.copy(fieldId = newKey, userId = currentUserId)
            databaseRef.child(newKey).setValue(fieldToSave)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _eventChannel.send(Event.ShowMessage("Successfully created new field: ${field.fieldName}!"))
                        _eventChannel.send(Event.FinishActivity)
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    viewModelScope.launch {
                        _eventChannel.send(Event.ShowMessage("Failed to create field!"))
                    }
                    _isLoading.value = false
                }
        } else {
            _isLoading.value = false
        }
    }

    fun updateField(field: Field) {
        if (currentUserId.isEmpty()) {
            viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Please log in first to update field!")) }
            return
        }

        if (field.userId != currentUserId) {
            viewModelScope.launch { _eventChannel.send(Event.ShowMessage("You don't have access to this field!")) }
            return
        }

        _isLoading.value = true
        val fieldId = field.fieldId

        if (fieldId != "") {
            databaseRef.child(fieldId).setValue(field)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _eventChannel.send(FieldViewModel.Event.ShowMessage("Successfully updated the field!"))
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    viewModelScope.launch {
                        _eventChannel.send(FieldViewModel.Event.ShowMessage("Something went wrong while trying to update the field!"))
                    }
                    _isLoading.value = false
                }
        } else {
            _isLoading.value = false
        }
    }

    fun deleteField(field: Field, context: Context) {
        if (currentUserId.isEmpty() || field.userId != currentUserId) {
            viewModelScope.launch { _eventChannel.send(Event.ShowMessage("Please log in first to delete field!")) }
            return
        }

        _isLoading.value = true

        databaseRef.child(field.fieldId.toString()).removeValue()
            .addOnSuccessListener {
                deleteLocalImageFile(field.fieldPhotoPath, context)
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowMessage("Successfully deleted field: ${field.fieldName}!"))
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("FieldViewModel", "Failed to delete field: ${e.message}!")
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowMessage("Failed to delete field!"))
                }
                _isLoading.value = false
            }
    }

    private fun deleteLocalImageFile(path: String?, context: Context) {
        if (path == null) return

        try {
            val fileToDelete = File(path)
            if (fileToDelete.exists()) {
                val wasDeleted = fileToDelete.delete()
                if (wasDeleted) {
                    Log.d("FieldViewModel", "Local image deleted successfully: $path")
                } else {
                    Log.w("FieldViewModel", "Local image could not be deleted: $path")
                }
            } else {
                Log.w("FieldViewModel", "Local image file not found: $path")
            }
        } catch (e: Exception) {
            Log.e("FieldViewModel", "Error deleting local file: ${e.message}")
        }
    }

    fun populateData() {
        val fieldsList = mutableListOf<Field>()

        fieldsList.add(
            Field(
                fieldId = "Field1",
                fieldPhotoPath = "ada",
                fieldName = "Lahan 1",
                fieldArea = 1270.0,
                fieldLocation = FieldLocation(
                    101.10, 101.10, "Ngabang"
                ),
                avgOilPalmAgeInMonths = 12,
                oilPalmType = "Tenera",
                fieldDesc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent ut hendrerit felis. Aliquam ut odio enim. Vestibulum convallis convallis bibendum. Ut id purus vel libero porttitor volutpat. Pellentesque placerat finibus arcu quis congue. Aenean et eros ac dolor pharetra placerat quis pharetra quam. Donec fermentum consectetur aliquet. Vestibulum commodo velit ac dui gravida, efficitur malesuada ligula scelerisque. Pellentesque rhoncus scelerisque risus quis efficitur. Phasellus placerat lorem lectus, non malesuada nunc facilisis a. Quisque ullamcorper enim sit amet lacus convallis rutrum. Quisque venenatis sapien ac dolor dapibus, eget pretium purus eleifend. Fusce pellentesque velit in tellus finibus consectetur eget blandit purus.",
            )
        )

        fieldsList.add(
            Field(
                fieldId = "Field2",
                fieldPhotoPath = null,
                fieldName = "Lahan Manjur Sukses",
                fieldArea = 550.10,
                fieldLocation = FieldLocation(
                    101.10, 101.10, "Sosok"
                ),
                avgOilPalmAgeInMonths = 37,
                oilPalmType = "Topaz",
                fieldDesc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ullamcorper elit mauris, eu lacinia nunc scelerisque a. Nam a tincidunt lacus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.",
            )
        )
        _fieldsData.value = fieldsList.take(3)
    }
}