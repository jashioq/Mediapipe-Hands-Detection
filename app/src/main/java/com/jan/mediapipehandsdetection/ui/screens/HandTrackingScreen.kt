package com.jan.mediapipehandsdetection.ui.screens

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.jan.mediapipehandsdetection.HandTrackingViewModel
import com.jan.mediapipehandsdetection.camera.CameraManager
import com.jan.mediapipehandsdetection.ui.components.CameraPermissionDenied
import com.jan.mediapipehandsdetection.ui.components.HandLandmarkOverlay
import com.jan.mediapipehandsdetection.ui.components.SettingsBottomSheet
import com.jan.mediapipehandsdetection.ui.components.TransparentIconButton

/**
 * Main screen for hand tracking
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandTrackingScreen(
    viewModel: HandTrackingViewModel,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            HandTrackingContent(viewModel = viewModel, modifier = modifier)
        }
        else -> {
            CameraPermissionDenied(
                permissionState = cameraPermissionState,
                modifier = modifier
            )
        }
    }
}

/**
 * Main content for hand tracking screen
 */
@Composable
private fun HandTrackingContent(
    viewModel: HandTrackingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraManager by remember { mutableStateOf<CameraManager?>(null) }
    var viewSize by remember { mutableStateOf(Pair(0f, 0f)) }

    // Initialize MediaPipe
    LaunchedEffect(Unit) {
        viewModel.initializeHandLandmarker(context)
    }

    // Initialize camera when camera facing changes
    LaunchedEffect(uiState.cameraFacing) {
        cameraManager?.switchCamera()
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager?.shutdown()
        }
    }

    Box(modifier = modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            viewSize = Pair(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
        }
    ) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE

                    previewView = this

                    // Initialize camera manager
                    cameraManager = CameraManager(
                        context = ctx,
                        previewView = this,
                        lifecycleOwner = lifecycleOwner,
                        onFrameAnalyzed = { imageProxy ->
                            viewModel.processFrame(imageProxy)
                        },
                        initialLensFacing = uiState.cameraFacing
                    ).also { it.startCamera() }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Hand landmark overlay
        HandLandmarkOverlay(
            detectedHands = uiState.detectedHands,
            viewWidth = viewSize.first,
            viewHeight = viewSize.second,
            isFrontCamera = uiState.cameraFacing == androidx.camera.core.CameraSelector.LENS_FACING_FRONT,
            modifier = Modifier.fillMaxSize()
        )

        // UI Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top row with controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Settings and Camera Switch buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransparentIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = { viewModel.toggleSettingsSheet() }
                    )

                    TransparentIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Switch Camera",
                        onClick = { viewModel.toggleCamera() }
                    )
                }

                // FPS Counter
                Text(
                    text = "FPS: ${uiState.fps}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Settings Bottom Sheet
        if (uiState.showSettingsSheet) {
            SettingsBottomSheet(
                config = uiState.config,
                onDismiss = { viewModel.toggleSettingsSheet() },
                onConfigUpdate = { newConfig ->
                    viewModel.updateConfig(newConfig, context)
                }
            )
        }
    }
}
