package com.example.sawit.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawit.models.Field
import com.example.sawit.models.FieldLocation
import com.example.sawit.models.User
import com.google.firebase.Firebase
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

    sealed class AuthEvent {
        data class Success(val user: User) : AuthEvent()
        data class Error(val message: String) : AuthEvent()
    }

    private val _authEvents = MutableStateFlow<AuthEvent?>(null)
    val authEvents: StateFlow<AuthEvent?> = _authEvents

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
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
                    saveNewUserProfile(uid, email, fullName)
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
                    _authEvents.value = AuthEvent.Success(userProfile)
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

    private fun saveNewUserProfile(uid: String, email: String, fullName: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val userProfile = User(
            uid = uid,
            fullName = fullName,
            email = email,
            createdAt = dateFormat.format(Date())
        )

        val profileRef = databaseRef.child(uid)

        profileRef.setValue(userProfile)
            .addOnSuccessListener {
                _authEvents.value = AuthEvent.Success(userProfile)
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                val authUser = auth.currentUser
                if (authUser != null && authUser.uid == uid) {
                    authUser.delete()
                        .addOnSuccessListener {
                            _authEvents.value =
                                AuthEvent.Error("Registration failed, profile save failed and account was deleted! Please try again.")
                            _isLoading.value = false
                        }
                        .addOnFailureListener { e ->
                            _authEvents.value =
                                AuthEvent.Error("Registration failed, please contact the developer!")
                            Log.e(
                                "UserViewModel",
                                "Account deletion failedError: ${e.localizedMessage}"
                            )
                            _isLoading.value = false
                        }
                } else {
                    _authEvents.value =
                        AuthEvent.Error("Registration failed (profile save failed).")
                    _isLoading.value = false
                }
            }
    }

    fun signOutAfterRegistration() = viewModelScope.launch {
        auth.signOut()
        _currentUser.value = null
        Log.d("UserViewModel", "User signed out immediately after registration!")
    }

    fun consumeAuthEvent() {
        _authEvents.value = null
    }
}