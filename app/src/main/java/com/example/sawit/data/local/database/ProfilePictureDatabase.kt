package com.example.sawit.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sawit.data.local.entity.ProfilePictureModel

@Database(
    entities = [ProfilePictureModel::class],
    version = 1,
    exportSchema = false
    )
abstract class ProfilePictureDatabase : RoomDatabase() {
}