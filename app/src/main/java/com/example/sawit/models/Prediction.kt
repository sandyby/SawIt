// File: com/example/sawit/models/Prediction.kt
package com.example.sawit.models

import com.google.firebase.firestore.DocumentId

data class Prediction(
    @DocumentId
    var id: String? = null,
    val userId: String? = null,
    val date: Long = System.currentTimeMillis(),
    val fieldName: String? = null,
    val predictionType: String? = null, // "Kondisi" atau "Total Panen"
    val predictedYield: Float = 0f,
    val actualYield: Float? = null, // Optional, hanya untuk Kondisi
    val conditionLabel: String? = null, // Optional, hanya untuk Kondisi
    val gapPercentage: Float? = null, // Optional, hanya untuk Kondisi
    val tmin: Float = 0f,
    val tmax: Float = 0f,
    val rainfall: Float = 0f,
    val area: Float = 0f
)