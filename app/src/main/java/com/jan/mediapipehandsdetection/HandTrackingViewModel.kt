package com.jan.mediapipehandsdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for hand tracking with MediaPipe
 */
class HandTrackingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HandTrackingUiState())
    val uiState: StateFlow<HandTrackingUiState> = _uiState.asStateFlow()

    private var handLandmarker: HandLandmarker? = null
    private var lastFrameTime = System.currentTimeMillis()
    private var frameCount = 0
    private var fpsUpdateTime = System.currentTimeMillis()

    companion object {
        private const val TAG = "HandTrackingViewModel"
    }

    fun initializeHandLandmarker(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val baseOptions = BaseOptions.builder()
                    .setModelAssetPath("hand_landmarker.task")
                    .build()

                val options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.VIDEO)
                    .setNumHands(_uiState.value.config.maxNumHands)
                    .setMinHandDetectionConfidence(_uiState.value.config.minHandDetectionConfidence)
                    .setMinHandPresenceConfidence(_uiState.value.config.minHandPresenceConfidence)
                    .setMinTrackingConfidence(_uiState.value.config.minTrackingConfidence)
                    .build()

                handLandmarker = HandLandmarker.createFromOptions(context, options)
                Log.d(TAG, "HandLandmarker initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing HandLandmarker", e)
                e.printStackTrace()
            }
        }
    }

    fun processFrame(imageProxy: ImageProxy) {
        if (handLandmarker == null) {
            Log.w(TAG, "HandLandmarker not initialized yet")
            imageProxy.close()
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val bitmap = imageProxyToBitmap(imageProxy)
                val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())

                val mpImage = BitmapImageBuilder(rotatedBitmap).build()
                // MediaPipe expects timestamp in milliseconds for VIDEO mode
                val timestamp = System.currentTimeMillis()

                val result = handLandmarker?.detectForVideo(mpImage, timestamp)

                if (result != null) {
                    if (result.landmarks().isNotEmpty()) {
                        Log.d(TAG, "Detected ${result.landmarks().size} hand(s) with ${result.landmarks()[0].size} landmarks each")
                    }
                    processHandLandmarkerResult(result, imageProxy.width, imageProxy.height)
                } else {
                    Log.w(TAG, "HandLandmarker returned null result")
                }

                updateFps()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame", e)
                e.printStackTrace()
            } finally {
                imageProxy.close()
            }
        }
    }

    private fun processHandLandmarkerResult(
        result: HandLandmarkerResult,
        imageWidth: Int,
        imageHeight: Int
    ) {
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

            Log.d(TAG, "Hand $index: ${landmarks.size} landmarks, handedness: $handedness")

            detectedHands.add(
                HandResult(
                    landmarks = landmarks,
                    handedness = handedness,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        _uiState.update { currentState ->
            currentState.copy(detectedHands = detectedHands)
        }
    }

    private fun updateFps() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - fpsUpdateTime

        if (elapsed >= 1000) {
            val fps = (frameCount * 1000 / elapsed).toInt()
            _uiState.update { it.copy(fps = fps) }
            frameCount = 0
            fpsUpdateTime = currentTime
        }
    }

    fun toggleCamera() {
        _uiState.update { currentState ->
            currentState.copy(
                cameraFacing = if (currentState.cameraFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
            )
        }
    }

    fun updateConfig(newConfig: HandTrackingConfig, context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            // Close existing landmarker
            handLandmarker?.close()
            handLandmarker = null

            // Update config
            _uiState.update { it.copy(config = newConfig) }

            // Reinitialize with new config
            try {
                val baseOptions = BaseOptions.builder()
                    .setModelAssetPath("hand_landmarker.task")
                    .build()

                val options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.VIDEO)
                    .setNumHands(newConfig.maxNumHands)
                    .setMinHandDetectionConfidence(newConfig.minHandDetectionConfidence)
                    .setMinHandPresenceConfidence(newConfig.minHandPresenceConfidence)
                    .setMinTrackingConfidence(newConfig.minTrackingConfidence)
                    .build()

                handLandmarker = HandLandmarker.createFromOptions(context, options)
                Log.d(TAG, "HandLandmarker reinitialized with new config")
            } catch (e: Exception) {
                Log.e(TAG, "Error reinitializing HandLandmarker", e)
                e.printStackTrace()
            }
        }
    }

    fun toggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet) }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val bitmap = Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )

        val buffer = imageProxy.planes[0].buffer
        buffer.rewind()

        bitmap.copyPixelsFromBuffer(buffer)

        return bitmap
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply {
            postRotate(degrees)
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    override fun onCleared() {
        super.onCleared()
        handLandmarker?.close()
    }
}

/**
 * UI state for hand tracking screen
 */
data class HandTrackingUiState(
    val detectedHands: List<HandResult> = emptyList(),
    val fps: Int = 0,
    val cameraFacing: Int = CameraSelector.LENS_FACING_BACK,
    val config: HandTrackingConfig = HandTrackingConfig(),
    val showSettingsSheet: Boolean = false
)
