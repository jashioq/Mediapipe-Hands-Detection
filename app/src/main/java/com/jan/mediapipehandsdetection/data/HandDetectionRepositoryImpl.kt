package com.jan.mediapipehandsdetection.data

import android.content.Context
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.jan.mediapipehandsdetection.models.HandResult
import com.jan.mediapipehandsdetection.models.HandTrackingConfig
import com.jan.mediapipehandsdetection.models.HandSide
import com.jan.mediapipehandsdetection.utils.ImageProcessingUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MediaPipe-based implementation of [HandDetectionRepository].
 *
 * Converts camera frames to bitmaps, performs hand detection using MediaPipe,
 * and emits results via StateFlow.
 */
class HandDetectionRepositoryImpl : HandDetectionRepository {

    private var handLandmarker: HandLandmarker? = null
    private var context: Context? = null

    private val _detectedHands = MutableStateFlow<List<HandResult>>(emptyList())
    override val detectedHands: StateFlow<List<HandResult>> = _detectedHands.asStateFlow()

    override suspend fun initialize(context: Context, config: HandTrackingConfig) {
        this.context = context
        handLandmarker = HandLandmarkerFactory.createHandLandmarker(context, config)
    }

    override suspend fun processFrame(imageProxy: ImageProxy) {
        val landmarker = handLandmarker ?: return

        val bitmap = ImageProcessingUtils.imageProxyToBitmap(imageProxy)
        val rotatedBitmap = ImageProcessingUtils.rotateBitmap(
            bitmap,
            imageProxy.imageInfo.rotationDegrees.toFloat()
        )

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        val timestamp = System.currentTimeMillis()
        val result = landmarker.detectForVideo(mpImage, timestamp)

        result?.let {
            _detectedHands.emit(processHandLandmarkerResult(it))
        }
    }

    override suspend fun updateConfig(config: HandTrackingConfig) {
        val ctx = context ?: return
        handLandmarker?.close()
        handLandmarker = HandLandmarkerFactory.createHandLandmarker(ctx, config)
    }

    private fun processHandLandmarkerResult(result: HandLandmarkerResult) =
        result.landmarks().mapIndexed { index, landmarks ->
            val handedness = result.handednesses()
                .getOrNull(index)
                ?.firstOrNull()
                ?.categoryName()
                ?.let { name ->
                    when (name.lowercase()) {
                        "left" -> HandSide.LEFT
                        else -> HandSide.RIGHT
                    }
                } ?: HandSide.RIGHT

            HandResult(landmarks, handedness)
        }
}
