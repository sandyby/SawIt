package com.example.sawit.models

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val activityId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)