package com.example.sawit.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawit.models.Field
import com.example.sawit.models.FieldLocation
import com.example.sawit.models.User
import com.example.sawit.utils.ImageCacheManager
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef =
        FirebaseDatabase.getInstance("https://sawit-6876f-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("users")
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()
    private lateinit var userListener: ValueEventListener
    private var isListenerInitialized: Boolean = false

    sealed class AuthEvent {
        data class Success(val user: User) : AuthEvent()
        data class Error(val message: String) : AuthEvent()
        object RegistrationSuccess: AuthEvent()
    }

    private val _authEvents = MutableStateFlow<AuthEvent?>(null)
    val authEvents: StateFlow<AuthEvent?> = _authEvents

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun listenForUserUpdates() {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty() || isListenerInitialized) {
            return
        }

        val userByIdQuery = databaseRef.child(userId)

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(User::class.java)

                if (userProfile != null) {
                    _userProfile.value = userProfile.copy(uid = userId)
                    Log.d("UserViewModel", "Real-time profile update received for UID: $userId")
                } else {
                    _userProfile.value = null
                    Log.w("UserViewModel", "Real-time update received null data for UID: $userId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserViewModel", "Database listener cancelled: ${error.message}")
            }
        }
        userByIdQuery.addValueEventListener(userListener)
        isListenerInitialized = true
        Log.d("UserViewModel", "ValueEventListener attached for UID: $userId")
    }

    override fun onCleared() {
        super.onCleared()
        if (isListenerInitialized && auth.currentUser != null) {
            databaseRef.child(auth.currentUser!!.uid).removeEventListener(userListener)
            isListenerInitialized = false
            Log.d("UserViewModel", "ValueEventListener removed.")
        }
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        _isLoading.value = true
        _authEvents.value = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    fetchUserProfile(uid)
                } else {
                    _authEvents.value =
                        AuthEvent.Error("Login successful, but user data is missing!")
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { exception ->
                _authEvents.value = AuthEvent.Error(exception.localizedMessage ?: "Login failed.")
                _isLoading.value = false
            }
    }

    fun registerUser(email: String, password: String, fullName: String) = viewModelScope.launch {
        _isLoading.value = true
        _authEvents.value = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    createNewUserProfile(uid, email, fullName)
//                    saveNewUserProfile(uid, email, fullName)
                } else {
                    _authEvents.value = AuthEvent.Error("Registration failed: no UID generated.")
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _authEvents.value =
                    AuthEvent.Error(e.localizedMessage ?: "Registration failed.")
                _isLoading.value = false
            }
    }

    fun logout() = viewModelScope.launch {
        auth.signOut()
        _authEvents.value = null
    }

    private fun fetchUserProfile(uid: String) {
        databaseRef.child(uid).get()
            .addOnSuccessListener { snapshot ->
                var userProfile = snapshot.getValue(User::class.java)
                if (snapshot.exists() && userProfile != null) {
                    userProfile = userProfile.copy(uid = uid)
                    _userProfile.value = userProfile
                    _authEvents.value = AuthEvent.Success(userProfile)
                    listenForUserUpdates()
                } else {
                    auth.signOut()
                    _authEvents.value =
                        AuthEvent.Error("Authentication success but profile data are missing!")
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _authEvents.value =
                    AuthEvent.Error("Failed to fetch profile: ${e.localizedMessage}")
                _isLoading.value = false
            }
    }

    fun updateExistingUserProfile(
        fullName: String,
        newProfilePhotoBase64: String? = null,
        newProfilePhotoLocalPath: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val existingUser = _userProfile.value

        if (uid.isNullOrEmpty() || existingUser == null) {
            _authEvents.value = AuthEvent.Error("Authentication session expired. Please log in again.")
            return
        }

        _isLoading.value = true

        val updatedUser = existingUser.copy(
            fullName = fullName,
            profilePhotoBase64 = newProfilePhotoBase64 ?: existingUser.profilePhotoBase64,
            profilePhotoLocalPath = newProfilePhotoLocalPath ?: existingUser.profilePhotoLocalPath
        )

        databaseRef.child(uid).setValue(updatedUser)
            .addOnSuccessListener {
                _isLoading.value = false
                _authEvents.value = AuthEvent.Success(updatedUser)
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Failed to update profile", e)
                _isLoading.value = false
                _authEvents.value = AuthEvent.Error("Failed to save profile changes!")
            }
    }

    fun saveNewUserProfile(
        uid: String, email: String, fullName: String, newProfilePhotoBase64: String? = null,
        newProfilePhotoLocalPath: String? = null
    ) {
        if (uid.isEmpty()) return
        _isLoading.value = true

        databaseRef.child(uid).get().addOnSuccessListener { snapshot ->
            val existingUser = snapshot.getValue(User::class.java)

            if (existingUser != null) {
                // Check if we are replacing an existing picture. If so, delete the old local file.
                if (newProfilePhotoLocalPath != null || newProfilePhotoBase64 != null) {
                    // If a new picture is being saved, delete the old local file if it exists.
                    // NOTE: This deletion should optimally happen when the picture is successfully saved.
                    // For simplicity here, we rely on the cache cleanup/overwrite.
                    // We trust the new local path will replace the old one.
                }

                val updatedUser = existingUser.copy(
                    fullName = fullName,
                    profilePhotoBase64 = newProfilePhotoBase64 ?: existingUser.profilePhotoBase64,
                    profilePhotoLocalPath = newProfilePhotoLocalPath
                        ?: existingUser.profilePhotoLocalPath
                )

                databaseRef.child(uid).setValue(updatedUser)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        _authEvents.value = AuthEvent.Success(updatedUser)
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserViewModel", "Failed to update profile", e)
                        _isLoading.value = false
                        _authEvents.value = AuthEvent.Error("Failed to save data!")
                    }
            } else {
                val newUser = User(
                    uid = uid,
                    fullName = fullName,
                    email = email,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    ),
                    profilePhotoBase64 = newProfilePhotoBase64,
                    profilePhotoLocalPath = newProfilePhotoLocalPath
                )

                databaseRef.child(uid).setValue(newUser)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        _authEvents.value = AuthEvent.Success(newUser)
                        Log.d("UserViewModel", "New profile created successfully for UID: $uid")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserViewModel", "Failed to create new profile", e)
                        _isLoading.value = false
                        _authEvents.value =
                            AuthEvent.Error("Registration failed: Could not save user data!")
                    }
            }
        }.addOnFailureListener {
            _isLoading.value = false
            _authEvents.value =
                AuthEvent.Error("Something went wrong while trying to update your profile!")
        }
    }

    private fun createNewUserProfile(
        uid: String, email: String, fullName: String, newProfilePhotoBase64: String? = null,
        newProfilePhotoLocalPath: String? = null
    ) {
        val newUser = User(
            uid = uid,
            fullName = fullName,
            email = email,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            profilePhotoBase64 = newProfilePhotoBase64,
            profilePhotoLocalPath = newProfilePhotoLocalPath
        )

        databaseRef.child(uid).setValue(newUser)
            .addOnSuccessListener {
                Log.d("UserViewModel", "New profile created successfully for UID: $uid. Signing out...")
                _authEvents.value = AuthEvent.RegistrationSuccess
                logout()
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Failed to create new profile", e)
                _isLoading.value = false
                _authEvents.value = AuthEvent.Error("Registration failed: Could not save user data!")
            }
    }

    fun signOutAfterRegistration() = viewModelScope.launch {
        auth.signOut()
        _currentUser.value = null
        Log.d("UserViewModel", "User signed out immediately after registration!")
    }

    fun updateImageLocalPath(newLocalPath: String) {
        viewModelScope.launch {
            userProfile.value?.let { currentUser ->
                val updatedUser = currentUser.copy(
                    profilePhotoLocalPath = newLocalPath,
                    profilePhotoBase64 = null
                )

                val updates = mapOf(
                    "profilePhotoLocalPath" to newLocalPath,
                    "profilePhotoBase64" to null
                )

                databaseRef.child(currentUser.uid).updateChildren(updates)
                    .addOnSuccessListener {
                        Log.d("UserViewModel", "Local cache path persisted successfully.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserViewModel", "Failed to persist local cache path: $e")
                    }
            }
        }
        //        if (uid.isEmpty()) return
//
//        val pathUpdate = mapOf("profilePhotoLocalPath" to newLocalPath)
//
//        databaseRef.child(uid).updateChildren(
//            pathUpdate
//        )
//            .addOnSuccessListener {
//                Log.d("UserViewModel", "Updated new local path: $newLocalPath")
//            }
//            .addOnFailureListener { e ->
//                Log.d("UserViewModel", "Failed to update the local path in the DB!")
//            }
    }

    fun updatePassword(oldPassword: String, newPassword: String) = viewModelScope.launch {
        _isLoading.value = true
        _authEvents.value = null

        val user = auth.currentUser
        if (user == null) {
            _authEvents.value = AuthEvent.Error("User not logged in. Please log in again.")
            _isLoading.value = false
            return@launch
        }

        val credential = EmailAuthProvider.getCredential(
            user.email ?: "",
            oldPassword
        )

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        _authEvents.value = AuthEvent.Success(
                            User(
                                uid = user.uid,
                                fullName = user.displayName ?: "",
                                email = user.email ?: ""
                            )
                        )
//                        refreshUserProfile()
                        Log.d("UserViewModel", "Password successfully updated.")
                        _isLoading.value = false
                    }
                    .addOnFailureListener { exception ->
                        _authEvents.value = AuthEvent.Error(
                            exception.localizedMessage ?: "Failed to update password."
                        )
                        _isLoading.value = false
                    }
            }
            .addOnFailureListener { exception ->
                _authEvents.value = AuthEvent.Error(
                    "Incorrect current password or login expired. Please check your current password."
                )
                _isLoading.value = false
            }
    }

    fun refreshUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            fetchUserProfile(uid)
        }
    }

    fun consumeAuthEvent() {
        _authEvents.value = null
    }
}