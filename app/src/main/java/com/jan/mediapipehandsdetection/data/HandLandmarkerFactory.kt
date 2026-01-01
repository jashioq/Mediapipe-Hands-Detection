package com.jan.mediapipehandsdetection.data

import android.content.Context
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.jan.mediapipehandsdetection.models.HandTrackingConfig

object HandLandmarkerFactory {

    /**
     * Creates a HandLandmarker configured for video mode
     *
     * @param context Android context
     * @param config Hand tracking configuration
     * @return Configured HandLandmarker instance
     */
    fun createHandLandmarker(
        context: Context,
        config: HandTrackingConfig
    ): HandLandmarker {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.VIDEO)
            .setNumHands(config.maxNumHands)
            .setMinHandDetectionConfidence(config.minHandDetectionConfidence)
            .setMinHandPresenceConfidence(config.minHandPresenceConfidence)
            .setMinTrackingConfidence(config.minTrackingConfidence)
            .build()

        return HandLandmarker.createFromOptions(context, options)
    }
}
