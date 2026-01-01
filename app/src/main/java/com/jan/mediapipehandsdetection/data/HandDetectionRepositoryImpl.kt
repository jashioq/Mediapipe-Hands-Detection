package com.jan.mediapipehandsdetection.data

import android.content.Context
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

class HandDetectionRepositoryImpl : HandDetectionRepository {

    private var handLandmarker: HandLandmarker? = null

    private val _detectedHands = MutableStateFlow<List<HandResult>>(emptyList())
    override val detectedHands: StateFlow<List<HandResult>> = _detectedHands.asStateFlow()

    override suspend fun initialize(context: Context, config: HandTrackingConfig) {
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

    override suspend fun updateConfig(context: Context, config: HandTrackingConfig) {
        handLandmarker?.close()
        handLandmarker = HandLandmarkerFactory.createHandLandmarker(context, config)
    }

    override fun close() {
        handLandmarker?.close()
        handLandmarker = null
    }

    private fun processHandLandmarkerResult(result: HandLandmarkerResult) =
        result.landmarks().mapIndexed { index, landmarks ->
            val handedness = result.handednesses()
                .getOrNull(index)
                ?.firstOrNull()
                ?.categoryName()
                ?.let { name ->
                    when (name.lowercase()) {
                        "left" -> HandType.LEFT
                        else -> HandType.RIGHT
                    }
                } ?: HandType.RIGHT

            HandResult(landmarks, handedness)
        }
}
