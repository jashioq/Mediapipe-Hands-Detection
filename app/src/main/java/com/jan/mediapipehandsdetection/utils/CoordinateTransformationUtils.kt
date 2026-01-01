package com.jan.mediapipehandsdetection.utils

/**
 * Utility functions for transforming landmark coordinates
 */
object CoordinateTransformationUtils {

    /**
     * Transforms a normalized landmark coordinate to screen coordinates,
     * accounting for camera mirroring and letterbox offsets
     *
     * @param normalizedValue The normalized coordinate value
     * @param previewSize The size of the preview area
     * @param offset The letterbox offset
     * @param isFrontCamera Whether the front camera is active
     * @param isXAxis Whether this is the X-axis coordinate
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
            if (!isFrontCamera) {
                offset + previewSize - (normalizedValue * previewSize)
            } else {
                offset + (normalizedValue * previewSize)
            }
        } else {
            offset + (normalizedValue * previewSize)
        }
    }
}
