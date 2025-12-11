// File: com/example/sawit/data/PredictionHistoryManager.kt
package com.example.sawit.data.roaming

import com.example.sawit.models.Prediction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.util.Log

class PredictionHistoryManager { // <--- Penamaan Baru

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserHistoryCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("predictions")
    }

    /** Menyimpan objek Prediction baru ke Firestore */
    fun savePrediction(prediction: Prediction) {
        val collection = getUserHistoryCollection()
        if (collection == null) {
            Log.e("PredictionHistoryManager", "Gagal menyimpan: Pengguna belum login atau UID null.")
            return
        }
        collection.add(prediction)
            .addOnSuccessListener { Log.d("PredictionHistoryManager", "Riwayat prediksi berhasil disimpan.") }
            .addOnFailureListener { e -> Log.e("PredictionHistoryManager", "Gagal menyimpan riwayat: ${e.message}", e) }
    }

    /** Mengambil semua riwayat secara realtime menggunakan Kotlin Flow */
    fun getAllHistory(): Flow<List<Prediction>> = callbackFlow {
        val collection = getUserHistoryCollection()

        if (collection == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val subscription = collection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val historyList = snapshot?.toObjects(Prediction::class.java) ?: emptyList()
                trySend(historyList)
            }
        awaitClose { subscription.remove() }
    }
}