package com.jan.mediapipehandsdetection.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

/**
 * Utility functions for image processing operations
 */
object ImageProcessingUtils {

    /**
     * Converts an ImageProxy to a Bitmap
     *
     * @param imageProxy The ImageProxy from CameraX
     * @return Bitmap in ARGB_8888 format
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
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

    /**
     * Rotates a bitmap by the specified degrees
     *
     * @param bitmap The bitmap to rotate
     * @param degrees The rotation angle in degrees
     * @return Rotated bitmap, or original if degrees is 0
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
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
}
