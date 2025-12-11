package com.example.sawit.ml

import android.content.Context
import ai.onnxruntime.* // Mengaktifkan ONNX Runtime

object ORTSessionHelper {

    private var env: OrtEnvironment? = null
    private var sessionCache = mutableMapOf<String, OrtSession>()

    fun loadModel(context: Context, assetPath: String): OrtSession {
        // Inisialisasi Environment
        val environment = env ?: OrtEnvironment.getEnvironment().also { env = it }

        // Ambil dari cache jika sudah dimuat (mempercepat)
        if (sessionCache.containsKey(assetPath)) {
            return sessionCache[assetPath]!!
        }

        // Baca byte model dari Assets
        val modelBytes = context.assets.open(assetPath).readBytes()

        // Buat sesi ONNX baru
        val session = environment.createSession(
            modelBytes,
            OrtSession.SessionOptions()
        )

        // Simpan ke cache
        sessionCache[assetPath] = session
        return session
    }
}