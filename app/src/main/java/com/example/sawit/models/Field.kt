package com.example.sawit.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "fields")
data class Field(
    @DocumentId
    @PrimaryKey
    var fieldId: String = "",
    val userId: String = "",
    val fieldPhotoPath: String? = null,
    val fieldName: String = "",
    val fieldArea: Double? = null,
    val fieldLocation: FieldLocation = FieldLocation(),
    val avgOilPalmAgeInMonths: Int? = null,
    val oilPalmType: String = "",
    val fieldDesc: String = "",
) : Parcelable {
    companion object {
        val ADD_PLACEHOLDER = Field(fieldId = "ADD_PLACEHOLDER", fieldName = "Add New Field", fieldLocation = FieldLocation())
    }
}

@Parcelize
data class FieldLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
) : Parcelable