package com.example.sawit.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sawit.data.local.entity.ProfilePictureModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfilePictureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfilePicture(imageModel: ProfilePictureModel)
    @Query("SELECT * FROM profile_pictures")
    fun getAllProfilePictures(): LiveData<List<ProfilePictureModel>>

}