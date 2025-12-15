package com.example.sawit.utils

import java.util.Locale

fun Int?.formatOilPalmAge(): String {
    val ageInMonths = this
    if (ageInMonths == null || ageInMonths <= 0) return "N/A"
    val years = ageInMonths / 12
    val remainingMonths = ageInMonths % 12
    return when {
        years > 0 && remainingMonths > 0 -> "$years yrs $remainingMonths mos"
        years > 0 -> "$years yrs"
        remainingMonths > 0 -> "$remainingMonths mos"
        else -> "N/A"
    }
}

fun Double?.formatFieldArea(): String {
    val area = this
    if (area == null || area <= 0.0) return "N/A"
    return String.format(Locale("id", "ID"), "%.1f", area)
}