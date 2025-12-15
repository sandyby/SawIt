package com.example.sawit.utils

import com.example.sawit.models.Activity
import com.example.sawit.models.ActivityStatus
import com.example.sawit.models.ActivityTimelineItem
import java.text.SimpleDateFormat
import java.util.Locale

fun Activity.toTimelineItem(): ActivityTimelineItem {
    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this.date)

    val uiStatus = when (this.status.lowercase()) {
        "completed", "done" -> ActivityStatus.COMPLETED
        "today", "in_progress" -> ActivityStatus.TODAY
        "planned", "scheduled" -> ActivityStatus.UPCOMING
        else -> ActivityStatus.UPCOMING
    }

    return ActivityTimelineItem(
        id = this.id ?: throw IllegalStateException("Activity ID is missing!"),
        fieldName = this.fieldName,
        activityTitle = this.activityType,
        date = formattedDate,
        status = uiStatus
    )
}