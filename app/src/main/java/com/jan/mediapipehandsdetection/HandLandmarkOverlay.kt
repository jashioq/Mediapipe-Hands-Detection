package com.jan.mediapipehandsdetection

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * Canvas overlay for drawing hand landmarks and connections
 */
@Composable
fun HandLandmarkOverlay(
    detectedHands: List<HandResult>,
    viewWidth: Float,
    viewHeight: Float,
    isFrontCamera: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (detectedHands.isNotEmpty()) {
        Log.d("HandLandmarkOverlay", "Drawing ${detectedHands.size} hand(s), view size: ${viewWidth}x${viewHeight}, frontCamera: $isFrontCamera")
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Calculate actual preview area
        // Camera is 4:3 in landscape, but becomes 3:4 in portrait (rotated)
        val isPortrait = size.height > size.width
        val cameraAspectRatio = if (isPortrait) 3f / 4f else 4f / 3f
        val screenAspectRatio = size.width / size.height

        val (previewWidth, previewHeight, offsetX, offsetY) = if (screenAspectRatio > cameraAspectRatio) {
            // Screen is wider than camera - letterbox on sides
            val scaledWidth = size.height * cameraAspectRatio
            val xOffset = (size.width - scaledWidth) / 2
            Quad(scaledWidth, size.height, xOffset, 0f)
        } else {
            // Screen is taller than camera - letterbox on top/bottom
            val scaledHeight = size.width / cameraAspectRatio
            val yOffset = (size.height - scaledHeight) / 2
            Quad(size.width, scaledHeight, 0f, yOffset)
        }

        detectedHands.forEach { handResult ->
            val color = when (handResult.handedness) {
                HandType.LEFT -> Color(0xFF00FF00) // Green for left hand
                HandType.RIGHT -> Color(0xFF0080FF) // Blue for right hand
            }

            // Draw connections first (so they appear behind landmarks)
            drawHandConnections(
                handResult = handResult,
                previewWidth = previewWidth,
                previewHeight = previewHeight,
                offsetX = offsetX,
                offsetY = offsetY,
                isFrontCamera = isFrontCamera,
                color = Color.White
            )

            // Draw landmarks on top
            drawHandLandmarks(
                handResult = handResult,
                previewWidth = previewWidth,
                previewHeight = previewHeight,
                offsetX = offsetX,
                offsetY = offsetY,
                isFrontCamera = isFrontCamera,
                color = color
            )
        }
    }
}

private data class Quad(val width: Float, val height: Float, val offsetX: Float, val offsetY: Float)

/**
 * Draw landmark connections
 */
private fun DrawScope.drawHandConnections(
    handResult: HandResult,
    previewWidth: Float,
    previewHeight: Float,
    offsetX: Float,
    offsetY: Float,
    isFrontCamera: Boolean,
    color: Color
) {
    val landmarks = handResult.landmarks

    HandLandmarkConnections.CONNECTIONS.forEach { (startIdx, endIdx) ->
        if (startIdx < landmarks.size && endIdx < landmarks.size) {
            val startLandmark = landmarks[startIdx]
            val endLandmark = landmarks[endIdx]

            // Mirror X coordinate for BACK camera (front camera preview is already mirrored)
            val startX = if (!isFrontCamera) {
                offsetX + previewWidth - (startLandmark.x() * previewWidth)
            } else {
                offsetX + (startLandmark.x() * previewWidth)
            }
            val startY = offsetY + (startLandmark.y() * previewHeight)

            val endX = if (!isFrontCamera) {
                offsetX + previewWidth - (endLandmark.x() * previewWidth)
            } else {
                offsetX + (endLandmark.x() * previewWidth)
            }
            val endY = offsetY + (endLandmark.y() * previewHeight)

            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * Draw hand landmarks as circles
 */
private fun DrawScope.drawHandLandmarks(
    handResult: HandResult,
    previewWidth: Float,
    previewHeight: Float,
    offsetX: Float,
    offsetY: Float,
    isFrontCamera: Boolean,
    color: Color
) {
    handResult.landmarks.forEach { landmark ->
        // Mirror X coordinate for BACK camera (front camera preview is already mirrored)
        val x = if (!isFrontCamera) {
            offsetX + previewWidth - (landmark.x() * previewWidth)
        } else {
            offsetX + (landmark.x() * previewWidth)
        }
        val y = offsetY + (landmark.y() * previewHeight)

        drawCircle(
            color = color,
            radius = 8.dp.toPx(),
            center = Offset(x, y)
        )
    }
}
