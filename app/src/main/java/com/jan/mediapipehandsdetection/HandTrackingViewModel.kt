package com.jan.mediapipehandsdetection

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jan.mediapipehandsdetection.data.HandDetectionRepository
import com.jan.mediapipehandsdetection.data.HandDetectionRepositoryImpl
import com.jan.mediapipehandsdetection.models.HandResult
import com.jan.mediapipehandsdetection.models.HandTrackingConfig
import com.jan.mediapipehandsdetection.utils.FpsCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for hand tracking screen
 *
 * Responsibilities:
 * - Manage UI state
 * - Handle user interactions
 * - Delegate hand detection to repository
 * - Track FPS
 */
class HandTrackingViewModel(
    private val repository: HandDetectionRepository = HandDetectionRepositoryImpl(),
    private val fpsCounter: FpsCounter = FpsCounter()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HandTrackingUiState())
    val uiState: StateFlow<HandTrackingUiState> = _uiState.asStateFlow()

    init {
        // Observe repository's detected hands flow
        viewModelScope.launch {
            repository.detectedHands.collect { hands ->
                _uiState.update { it.copy(detectedHands = hands) }
            }
        }
    }

    /**
     * Initialize the hand landmarker
     */
    fun initializeHandLandmarker(context: Context) {
        viewModelScope.launch {
            repository.initialize(context, _uiState.value.config)
        }
    }

    /**
     * Process a camera frame
     */
    fun processFrame(imageProxy: ImageProxy) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Process frame via repository (results come via flow)
                repository.processFrame(imageProxy)

                // Update FPS
                fpsCounter.recordFrame()
                _uiState.update { it.copy(fps = fpsCounter.getCurrentFps()) }
            } finally {
                imageProxy.close()
            }
        }
    }

    /**
     * Toggle between front and back camera
     */
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

    /**
     * Update hand tracking configuration
     */
    fun updateConfig(newConfig: HandTrackingConfig, context: Context) {
        viewModelScope.launch {
            repository.updateConfig(context, newConfig)
            _uiState.update { it.copy(config = newConfig) }
        }
    }

    /**
     * Toggle settings bottom sheet visibility
     */
    fun toggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet) }
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}

/**
 * UI state for hand tracking screen
 *
 * @property detectedHands List of currently detected hands
 * @property fps Current frames per second
 * @property cameraFacing Current camera facing (front or back)
 * @property config Hand tracking configuration
 * @property showSettingsSheet Whether settings sheet is visible
 */
data class HandTrackingUiState(
    val detectedHands: List<HandResult> = emptyList(),
    val fps: Int = 0,
    val cameraFacing: Int = CameraSelector.LENS_FACING_BACK,
    val config: HandTrackingConfig = HandTrackingConfig(),
    val showSettingsSheet: Boolean = false
)
