package com.example.sawit.models

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class PredictionHistory(
    @DocumentId
    var id: String? = null,
    val userId: String? = null,
    val date: Date = Date(),
    val fieldId: String? = null,
    val fieldName: String? = null,
    val predictionType: String? = null,
    val predictedYield: Float = 0f,
    val actualYield: Float? = null,
    val conditionLabel: String? = null,
    val gapPercentage: Float? = null,
    val tmin: Float = 0f,
    val tmax: Float = 0f,
    val rainfall: Float = 0f,
    val area: Float = 0f
): Parcelable {

}