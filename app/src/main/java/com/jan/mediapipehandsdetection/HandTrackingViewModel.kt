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

class HandTrackingViewModel(
    private val repository: HandDetectionRepository = HandDetectionRepositoryImpl(),
    private val fpsCounter: FpsCounter = FpsCounter()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HandTrackingUiState())
    val uiState: StateFlow<HandTrackingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.detectedHands.collect { hands ->
                _uiState.update { it.copy(detectedHands = hands) }
            }
        }
    }

    fun initializeHandLandmarker(context: Context) {
        viewModelScope.launch {
            repository.initialize(context, _uiState.value.config)
        }
    }

    fun processFrame(imageProxy: ImageProxy) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.processFrame(imageProxy)

                fpsCounter.recordFrame()
                _uiState.update { it.copy(fps = fpsCounter.getCurrentFps()) }
            } finally {
                imageProxy.close()
            }
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

    fun updateConfig(newConfig: HandTrackingConfig) {
        viewModelScope.launch {
            repository.updateConfig(newConfig)
            _uiState.update { it.copy(config = newConfig) }
        }
    }

    fun toggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet) }
    }
}

/**
 * UI state for hand tracking screen
 *
 * @property detectedHands List of currently detected hands
 * @property fps Current frames per second
 * @property cameraFacing Current camera facing mode (front or back)
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
