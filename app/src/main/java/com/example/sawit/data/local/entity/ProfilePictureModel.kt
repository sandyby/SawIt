package com.example.sawit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_pictures")
data class ProfilePictureModel(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val imageData: ByteArray
)