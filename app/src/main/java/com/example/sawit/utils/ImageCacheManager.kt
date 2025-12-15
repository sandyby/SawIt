package com.example.sawit.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageCacheManager {
    private const val TAG = "ImageCacheManager"
    private const val MAX_FILE_SIZE_KB = 500

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
//        return try {
//            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
//                val bytes = inputStream.readBytes()
//                Base64.encodeToString(bytes, Base64.NO_WRAP)
//            }
//        } catch (e: Exception) {
//            Log.e("ImageCacheManager", "Failed to convert URI to Base64", e)
//            null
//        }
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // 1. Compression and Scaling
            val outputStream = ByteArrayOutputStream()
            // Compress to JPEG, targeting a quality that limits file size (e.g., 80)
            var quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            // Optional: Loop to reduce quality if size exceeds limit
            while ((outputStream.toByteArray().size / 1024) > MAX_FILE_SIZE_KB && quality > 10) {
                outputStream.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            // Log final size
            Log.d(TAG, "Final Base64 size: ${outputStream.toByteArray().size / 1024} KB (Quality: $quality)")


            // 2. Base64 Encoding
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to Base64", e)
            null
        }
    }

    fun base64ToLocalCache(context: Context, base64String: String): String? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val fileName = "profile_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(decodedBytes)
            }
            Log.d(TAG, "Base64 decoded and saved to local path: ${file.absolutePath}")
            return file.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error converting Base64 to local file", e)
            null
        }
    //        val fileName = "fetch_${UUID.randomUUID()}.jpg"
//        val file = File(context.filesDir, fileName)
//
//        return try {
//            val imageBytes = Base64.decode(base64String, Base64.NO_WRAP)
//            FileOutputStream(file).use { outputStream ->
//                outputStream.write(imageBytes)
//            }
//            file.absolutePath
//        } catch (e: Exception) {
//            Log.e("ImageCacheManager", "Failed to decode Base64 and save locally", e)
//            null
//        }
    }

    fun isCached(localPath: String?): Boolean {
        if (localPath.isNullOrEmpty()) return false
        val file = File(localPath)
        return file.exists() && file.length() > 0
    //        return path?.let { File(it).exists() } == true
    }

    fun deleteLocalFile(localPath: String?) {
        if (localPath.isNullOrEmpty()) return
        val file = File(localPath)
        if (file.exists()) {
            file.delete()
        }
    //        if (path != null) {
//            File(path).delete()
//        }
    }
}