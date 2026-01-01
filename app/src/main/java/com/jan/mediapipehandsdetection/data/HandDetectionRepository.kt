package com.jan.mediapipehandsdetection.data

import android.content.Context
import androidx.camera.core.ImageProxy
import com.jan.mediapipehandsdetection.models.HandResult
import com.jan.mediapipehandsdetection.models.HandTrackingConfig
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for hand detection using MediaPipe.
 */
interface HandDetectionRepository {
    /**
     * Flow of detected hands from processed frames.
     * Emits empty list when no hands are detected.
     */
    val detectedHands: StateFlow<List<HandResult>>

    /**
     * Initializes the MediaPipe hand landmarker with the given configuration.
     * Must be called before [processFrame].
     *
     * @param context Android context for accessing assets
     * @param config Hand tracking configuration parameters
     */
    suspend fun initialize(context: Context, config: HandTrackingConfig)

    /**
     * Processes a camera frame to detect hands.
     * Results are emitted asynchronously via [detectedHands] Flow.
     *
     * @param imageProxy Camera frame from CameraX
     */
    suspend fun processFrame(imageProxy: ImageProxy)

    /**
     * Updates the hand tracking configuration and reinitializes the landmarker.
     *
     * @param config New hand tracking configuration
     */
    suspend fun updateConfig(config: HandTrackingConfig)
}
