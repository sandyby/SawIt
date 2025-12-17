package com.example.sawit.utils

import com.example.sawit.models.Activity
import com.example.sawit.models.ActivityStatus
import com.example.sawit.models.ActivityTimelineItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Activity.toTimelineItem(): ActivityTimelineItem {
    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this.date)

    val calendarActivity = Calendar.getInstance().apply { time = this@toTimelineItem.date }
    val calendarToday = Calendar.getInstance()

    val isSameDay = calendarActivity.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR) &&
            calendarActivity.get(Calendar.DAY_OF_YEAR) == calendarToday.get(Calendar.DAY_OF_YEAR)

    val isFuture = this.date.after(Date()) && !isSameDay

    val uiStatus = when {
        this.status.lowercase() == "completed" || this.status.lowercase() == "done" -> ActivityStatus.COMPLETED
        isSameDay -> ActivityStatus.TODAY
        isFuture -> ActivityStatus.UPCOMING
        else -> ActivityStatus.COMPLETED
    }

    return ActivityTimelineItem(
        id = this.id ?: "",
        fieldName = this.fieldName,
        activityTitle = this.activityType,
        date = formattedDate,
        status = uiStatus
    )
}