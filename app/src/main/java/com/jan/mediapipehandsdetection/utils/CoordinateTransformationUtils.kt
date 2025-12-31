package com.jan.mediapipehandsdetection.utils

/**
 * Utility functions for transforming landmark coordinates
 */
object CoordinateTransformationUtils {

    /**
     * Transforms a normalized landmark coordinate to screen coordinates,
     * accounting for camera mirroring and letterbox offsets
     *
     * @param normalizedValue The normalized coordinate value (0.0 to 1.0)
     * @param previewSize The size of the preview area (width or height)
     * @param offset The letterbox offset (x or y)
     * @param isFrontCamera Whether the front camera is active
     * @param isXAxis Whether this is the X-axis (horizontal) coordinate
     * @return The transformed screen coordinate
     */
    fun transformLandmarkCoordinate(
        normalizedValue: Float,
        previewSize: Float,
        offset: Float,
        isFrontCamera: Boolean,
        isXAxis: Boolean
    ): Float {
        return if (isXAxis) {
            // Mirror X coordinate for BACK camera (front camera preview is already mirrored)
            if (!isFrontCamera) {
                offset + previewSize - (normalizedValue * previewSize)
            } else {
                offset + (normalizedValue * previewSize)
            }
        } else {
            // Y coordinate doesn't need mirroring
            offset + (normalizedValue * previewSize)
        }
    }
}
