package com.example.sawit.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import android.util.Base64

object ImageCacheManager {
    fun getLocalFilePath(context: Context, name: String): String {
        val fileName = "cache_${name}_${UUID.randomUUID()}.jpg"
        return File(context.filesDir, fileName).absolutePath
    }

    fun saveUriToLocalCache(context: Context, imageUri: Uri): String? {
        val fileName = "upload_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        return try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ImageCacheManager", "Failed to save image locally from URI", e)
            null
        }
    }

    fun uriToBase64(context: Context, imageUri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            Log.e("ImageCacheManager", "Failed to convert URI to Base64", e)
            null
        }
    }

    fun base64ToLocalCache(context: Context, base64String: String): String? {
        val fileName = "fetch_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        return try {
            val imageBytes = Base64.decode(base64String, Base64.NO_WRAP)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(imageBytes)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ImageCacheManager", "Failed to decode Base64 and save locally", e)
            null
        }
    }

    fun isCached(path: String?): Boolean {
        return path?.let { File(it).exists() } == true
    }

    fun deleteLocalFile(path: String?) {
        if (path != null) {
            File(path).delete()
        }
    }
}