package com.example.sawit.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class NotificationViewModel : ViewModel() {
    private val _notificationCount = MutableLiveData(0)
    val notificationCount: LiveData<Int> = _notificationCount

    private val auth = FirebaseAuth.getInstance()
    private val database =
        FirebaseDatabase.getInstance("https://sawit-6876f-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private var currentQuery: Query? = null
    private var notificationListener: ValueEventListener? = null

    fun listenForNotifications() {
        val userId = auth.currentUser?.uid ?: return
        val ref = database.getReference("notifications").child(userId)
        val unreadQuery = ref.orderByChild("isRead").equalTo(false)

        notificationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("NotificationDebug", "Total nodes found: ${snapshot.childrenCount}")
                for (child in snapshot.children) {
                    Log.d("NotificationDebug", "ID: ${child.key}, isRead: ${child.child("isRead").value}")
                }
                _notificationCount.postValue(snapshot.childrenCount.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        }
        unreadQuery.addValueEventListener(notificationListener!!)
    }

    fun stopListening() {
        notificationListener?.let {
            currentQuery?.removeEventListener(it)
            notificationListener = null
            currentQuery = null
        }
    }

    fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        val ref = database.getReference("notifications").child(userId)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val updates = mutableMapOf<String, Any?>()
                snapshot.children.forEach { child ->
                    if (child.child("isRead").getValue(Boolean::class.java) == false) {
                        updates["${child.key}/isRead"] = true
                    }
                }
                if (updates.isNotEmpty()) {
                    ref.updateChildren(updates)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}