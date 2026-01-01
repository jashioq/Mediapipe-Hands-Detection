package com.jan.mediapipehandsdetection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jan.mediapipehandsdetection.ui.screens.HandTrackingScreen
import com.jan.mediapipehandsdetection.ui.theme.MediaPipeHandsDetectionTheme

/**
 * Main activity for MediaPipe Hands Detection app
 */
class MainActivity : ComponentActivity() {

    private val viewModel: HandTrackingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemBars()

        setContent {
            MediaPipeHandsDetectionTheme {
                HandTrackingScreen(
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}