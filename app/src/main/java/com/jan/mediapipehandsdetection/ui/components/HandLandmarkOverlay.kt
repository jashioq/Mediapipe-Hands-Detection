package com.jan.mediapipehandsdetection.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.jan.mediapipehandsdetection.models.HandResult
import com.jan.mediapipehandsdetection.models.HandSide
import com.jan.mediapipehandsdetection.models.HandLandmarkConnections
import com.jan.mediapipehandsdetection.utils.AspectRatioUtils
import com.jan.mediapipehandsdetection.utils.CoordinateTransformationUtils
import com.jan.mediapipehandsdetection.utils.LetterboxBounds

/**
 * Drawing constants for hand landmarks
 */
private object DrawingConstants {
    val LANDMARK_RADIUS = 4.dp
    val CONNECTION_STROKE_WIDTH = 2.dp
}

/**
 * Canvas overlay for drawing hand landmarks and connections
 *
 * @param detectedHands List of detected hands to draw
 * @param viewWidth Width of the view
 * @param viewHeight Height of the view
 * @param isFrontCamera Whether the front camera is active
 */
@Composable
fun HandLandmarkOverlay(
    modifier: Modifier = Modifier,
    detectedHands: List<HandResult>,
    viewWidth: Float,
    viewHeight: Float,
    isFrontCamera: Boolean = false,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Calculate letterbox bounds for camera preview
        val isPortrait = size.height > size.width
        val cameraAspectRatio = if (isPortrait) 3f / 4f else 4f / 3f
        val bounds = AspectRatioUtils.calculateLetterboxBounds(size, cameraAspectRatio)

        detectedHands.forEach { handResult ->
            val color = when (handResult.handedSide) {
                HandSide.LEFT -> Color(0xFFFF5100) // Red for left hand
                HandSide.RIGHT -> Color(0xFF0080FF) // Blue for right hand
            }

            // Draw connections first (so they appear behind landmarks)
            drawHandConnections(
                handResult = handResult,
                bounds = bounds,
                isFrontCamera = isFrontCamera,
                color = Color.White
            )

            // Draw landmarks on top
            drawHandLandmarks(
                handResult = handResult,
                bounds = bounds,
                isFrontCamera = isFrontCamera,
                color = color
            )
        }
    }
}

private fun DrawScope.drawHandConnections(
    handResult: HandResult,
    bounds: LetterboxBounds,
    isFrontCamera: Boolean,
    color: Color
) {
    val landmarks = handResult.landmarks

    HandLandmarkConnections.CONNECTIONS.forEach { (startIdx, endIdx) ->
        if (startIdx < landmarks.size && endIdx < landmarks.size) {
            val startLandmark = landmarks[startIdx]
            val endLandmark = landmarks[endIdx]

            val startX = CoordinateTransformationUtils.transformLandmarkCoordinate(
                normalizedValue = startLandmark.x(),
                previewSize = bounds.contentWidth,
                offset = bounds.offsetX,
                isFrontCamera = isFrontCamera,
                isXAxis = true
            )
            val startY = CoordinateTransformationUtils.transformLandmarkCoordinate(
                normalizedValue = startLandmark.y(),
                previewSize = bounds.contentHeight,
                offset = bounds.offsetY,
                isFrontCamera = isFrontCamera,
                isXAxis = false
            )

            val endX = CoordinateTransformationUtils.transformLandmarkCoordinate(
                normalizedValue = endLandmark.x(),
                previewSize = bounds.contentWidth,
                offset = bounds.offsetX,
                isFrontCamera = isFrontCamera,
                isXAxis = true
            )
            val endY = CoordinateTransformationUtils.transformLandmarkCoordinate(
                normalizedValue = endLandmark.y(),
                previewSize = bounds.contentHeight,
                offset = bounds.offsetY,
                isFrontCamera = isFrontCamera,
                isXAxis = false
            )

            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = DrawingConstants.CONNECTION_STROKE_WIDTH.toPx()
            )
        }
    }
}

private fun DrawScope.drawHandLandmarks(
    handResult: HandResult,
    bounds: LetterboxBounds,
    isFrontCamera: Boolean,
    color: Color
) {
    handResult.landmarks.forEach { landmark ->
        val x = CoordinateTransformationUtils.transformLandmarkCoordinate(
            normalizedValue = landmark.x(),
            previewSize = bounds.contentWidth,
            offset = bounds.offsetX,
            isFrontCamera = isFrontCamera,
            isXAxis = true
        )
        val y = CoordinateTransformationUtils.transformLandmarkCoordinate(
            normalizedValue = landmark.y(),
            previewSize = bounds.contentHeight,
            offset = bounds.offsetY,
            isFrontCamera = isFrontCamera,
            isXAxis = false
        )

        drawCircle(
            color = color,
            radius = DrawingConstants.LANDMARK_RADIUS.toPx(),
            center = Offset(x, y)
        )
    }
}
