package com.jan.mediapipehandsdetection.models

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

/**
 * Represents hand tracking results for a single hand
 *
 * @property landmarks List of 21 hand landmarks
 * @property handedness Whether the hand is left or right
 * @property timestamp When this result was detected
 */
data class HandResult(
    val landmarks: List<NormalizedLandmark>,
    val handedness: HandType,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Hand type (left or right)
 */
enum class HandType {
    LEFT,
    RIGHT
}

/**
 * Hand landmark connections for visualization
 */
object HandLandmarkConnections {
    val CONNECTIONS = listOf(
        // Thumb
        0 to 1, 1 to 2, 2 to 3, 3 to 4,
        // Index finger
        0 to 5, 5 to 6, 6 to 7, 7 to 8,
        // Middle finger
        5 to 9, 9 to 10, 10 to 11, 11 to 12,
        // Ring finger
        9 to 13, 13 to 14, 14 to 15, 15 to 16,
        // Pinky
        13 to 17, 17 to 18, 18 to 19, 19 to 20,
        // Palm
        0 to 17
    )
}
