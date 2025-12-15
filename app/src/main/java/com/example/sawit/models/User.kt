package com.example.sawit.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val createdAt: String = "",
    val profilePhotoBase64: String? = null,
    val profilePhotoLocalPath: String? = null
) : Parcelable {
    constructor() : this(
        uid = "",
        fullName = "",
        email = "",
        createdAt = "",
        profilePhotoBase64 = null,
        profilePhotoLocalPath = null
    )
}
