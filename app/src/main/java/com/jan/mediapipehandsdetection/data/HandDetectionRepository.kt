package com.jan.mediapipehandsdetection.data

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.jan.mediapipehandsdetection.models.HandResult
import com.jan.mediapipehandsdetection.models.HandTrackingConfig
import com.jan.mediapipehandsdetection.models.HandType
import com.jan.mediapipehandsdetection.utils.ImageProcessingUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for hand detection using MediaPipe
 */
interface HandDetectionRepository {
    /** Flow of detected hands */
    val detectedHands: StateFlow<List<HandResult>>

    /**
     * Initialize the hand landmarker with the given configuration
     */
    suspend fun initialize(context: Context, config: HandTrackingConfig)

    /**
     * Process a camera frame and emit detected hands via flow
     */
    suspend fun processFrame(imageProxy: ImageProxy)

    /**
     * Update configuration and reinitialize the hand landmarker
     */
    suspend fun updateConfig(context: Context, config: HandTrackingConfig)

    /**
     * Close the hand landmarker and release resources
     */
    fun close()
}

/**
 * Implementation of HandDetectionRepository using MediaPipe
 */
class HandDetectionRepositoryImpl : HandDetectionRepository {

    private var handLandmarker: HandLandmarker? = null

    private val _detectedHands = MutableStateFlow<List<HandResult>>(emptyList())
    override val detectedHands: StateFlow<List<HandResult>> = _detectedHands.asStateFlow()

    companion object {
        private const val TAG = "HandDetectionRepository"
    }

    override suspend fun initialize(context: Context, config: HandTrackingConfig) {
        try {
            handLandmarker = HandLandmarkerFactory.createHandLandmarker(context, config)
            Log.d(TAG, "HandLandmarker initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing HandLandmarker", e)
            throw e
        }
    }

    override suspend fun processFrame(imageProxy: ImageProxy) {
        try {
            val landmarker = handLandmarker
            if (landmarker == null) {
                Log.w(TAG, "HandLandmarker not initialized yet")
                return
            }

            // Convert and rotate image
            val bitmap = ImageProcessingUtils.imageProxyToBitmap(imageProxy)
            val rotatedBitmap = ImageProcessingUtils.rotateBitmap(
                bitmap,
                imageProxy.imageInfo.rotationDegrees.toFloat()
            )

            // Detect hands
            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            val timestamp = System.currentTimeMillis()
            val result = landmarker.detectForVideo(mpImage, timestamp)

            // Process and emit results
            if (result != null) {
                val hands = processHandLandmarkerResult(result)
                _detectedHands.emit(hands)

                if (hands.isNotEmpty()) {
                    Log.d(TAG, "Detected ${hands.size} hand(s)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
        }
    }

    override suspend fun updateConfig(context: Context, config: HandTrackingConfig) {
        try {
            // Close existing landmarker
            handLandmarker?.close()
            handLandmarker = null

            // Reinitialize with new config
            handLandmarker = HandLandmarkerFactory.createHandLandmarker(context, config)
            Log.d(TAG, "HandLandmarker reinitialized with new config")
        } catch (e: Exception) {
            Log.e(TAG, "Error reinitializing HandLandmarker", e)
            throw e
        }
    }

    override fun close() {
        handLandmarker?.close()
        handLandmarker = null
    }

    /**
     * Converts MediaPipe detection result to app domain models
     */
    private fun processHandLandmarkerResult(result: HandLandmarkerResult): List<HandResult> {
        val detectedHands = mutableListOf<HandResult>()

        result.landmarks().forEachIndexed { index, landmarks ->
            val handedness = if (index < result.handednesses().size) {
                val categoryName = result.handednesses()[index].firstOrNull()?.categoryName()
                when (categoryName?.lowercase()) {
                    "left" -> HandType.LEFT
                    "right" -> HandType.RIGHT
                    else -> HandType.RIGHT
                }
            } else {
                HandType.RIGHT
            }

            detectedHands.add(
                HandResult(
                    landmarks = landmarks,
                    handedness = handedness,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        return detectedHands
    }
}
