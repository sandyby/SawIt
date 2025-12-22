package com.example.sawit.ml

import android.content.Context
import ai.onnxruntime.*

object ORTSessionHelper {

    private var env: OrtEnvironment? = null
    private var sessionCache = mutableMapOf<String, OrtSession>()

    fun loadModel(context: Context, assetPath: String): OrtSession {
        val environment = env ?: OrtEnvironment.getEnvironment().also { env = it }

        if (sessionCache.containsKey(assetPath)) {
            return sessionCache[assetPath]!!
        }

        val modelBytes = context.assets.open(assetPath).readBytes()

        val session = environment.createSession(
            modelBytes,
            OrtSession.SessionOptions()
        )

        sessionCache[assetPath] = session
        return session
    }
}