package com.jan.mediapipehandsdetection.models

/**
 * Configuration for hand tracking
 *
 * @property minHandDetectionConfidence Minimum confidence for hand detection (0.0-1.0)
 * @property minHandPresenceConfidence Minimum confidence for hand presence (0.0-1.0)
 * @property minTrackingConfidence Minimum confidence for landmark tracking (0.0-1.0)
 * @property maxNumHands Maximum number of hands to detect (1-2)
 */
data class HandTrackingConfig(
    val minHandDetectionConfidence: Float = 0.5f,
    val minHandPresenceConfidence: Float = 0.5f,
    val minTrackingConfidence: Float = 0.5f,
    val maxNumHands: Int = 2
)
