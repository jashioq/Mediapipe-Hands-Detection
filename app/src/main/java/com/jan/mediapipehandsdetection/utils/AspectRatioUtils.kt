package com.jan.mediapipehandsdetection.utils

import androidx.compose.ui.geometry.Size

/**
 * Represents the bounds of content within a letterboxed container
 *
 * @property contentWidth The width of the actual content area
 * @property contentHeight The height of the actual content area
 * @property offsetX The horizontal offset (black bars on sides)
 * @property offsetY The vertical offset (black bars on top/bottom)
 */
data class LetterboxBounds(
    val contentWidth: Float,
    val contentHeight: Float,
    val offsetX: Float,
    val offsetY: Float
)

/**
 * Utility functions for aspect ratio and letterbox calculations
 */
object AspectRatioUtils {

    /**
     * Calculates the actual content bounds when fitting content with a specific
     * aspect ratio into a container, accounting for letterboxing.
     *
     * When content is displayed with FIT_CENTER scaling,
     * black bars appear to maintain the aspect ratio. This function calculates
     * where the actual content sits within the container.
     *
     * @param containerSize The size of the container
     * @param contentAspectRatio The aspect ratio of the content
     * @return Bounds of the content area including letterbox offsets
     */
    fun calculateLetterboxBounds(
        containerSize: Size,
        contentAspectRatio: Float
    ): LetterboxBounds {
        val containerAspectRatio = containerSize.width / containerSize.height

        return if (containerAspectRatio > contentAspectRatio) {
            // Container is wider than content - bars on sides
            val scaledWidth = containerSize.height * contentAspectRatio
            val xOffset = (containerSize.width - scaledWidth) / 2
            LetterboxBounds(
                contentWidth = scaledWidth,
                contentHeight = containerSize.height,
                offsetX = xOffset,
                offsetY = 0f
            )
        } else {
            // Container is taller than content - bars on top and bottom
            val scaledHeight = containerSize.width / contentAspectRatio
            val yOffset = (containerSize.height - scaledHeight) / 2
            LetterboxBounds(
                contentWidth = containerSize.width,
                contentHeight = scaledHeight,
                offsetX = 0f,
                offsetY = yOffset
            )
        }
    }
}
