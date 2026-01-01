package com.jan.mediapipehandsdetection.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Provides camera initialization, switching, and frame delivery to
 * the hand detection pipeline.
 *
 * @property context Android context
 * @property previewView Surface for camera preview display
 * @property lifecycleOwner Lifecycle owner for camera binding
 * @property onFrameAnalyzed Callback invoked for each analyzed frame
 * @property initialLensFacing Initial camera facing direction (front/back)
 */
class CameraManager(
    private val context: Context,
    private val previewView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val onFrameAnalyzed: (ImageProxy) -> Unit,
    initialLensFacing: Int = CameraSelector.LENS_FACING_BACK
) {
    private lateinit var cameraProvider: ProcessCameraProvider
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var lensFacing = initialLensFacing

    /**
     * Initializes and starts the camera with current configuration.
     */
    fun startCamera() {
        ProcessCameraProvider.getInstance(context).apply {
            addListener({
                cameraProvider = get()
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(context))
        }
    }

    /**
     * Switches between front and back camera.
     * Rebinds camera use cases with the new camera facing mode.
     */
    fun switchCamera() {
        lensFacing = when (lensFacing) {
            CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_FRONT
            else -> CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCases()
    }

    private fun bindCameraUseCases() {
        if (!::cameraProvider.isInitialized) return

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            createCameraSelector(),
            createPreview(),
            createImageAnalyzer()
        )
    }

    private fun createCameraSelector() = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    private fun createPreview() = Preview.Builder()
        .build()
        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

    private fun createImageAnalyzer() = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .build()
        .also { it.setAnalyzer(cameraExecutor, onFrameAnalyzed) }
}
