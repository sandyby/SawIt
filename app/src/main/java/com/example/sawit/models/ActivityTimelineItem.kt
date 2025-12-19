package com.example.sawit.models

data class ActivityTimelineItem(
    val id: String,
    val fieldName: String,
    val activityTitle: String,
    val date: String,
    val status: ActivityStatus
)

enum class ActivityStatus{
    UPCOMING,
    TODAY,
    COMPLETED,
    OVERDUE
}