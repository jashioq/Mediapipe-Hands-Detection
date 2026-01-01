package com.jan.mediapipehandsdetection.data

import android.content.Context
import androidx.camera.core.ImageProxy
import com.jan.mediapipehandsdetection.models.HandResult
import com.jan.mediapipehandsdetection.models.HandTrackingConfig
import kotlinx.coroutines.flow.StateFlow

interface HandDetectionRepository {
    val detectedHands: StateFlow<List<HandResult>>

    suspend fun initialize(context: Context, config: HandTrackingConfig)
    suspend fun processFrame(imageProxy: ImageProxy)
    suspend fun updateConfig(context: Context, config: HandTrackingConfig)
    fun close()
}
