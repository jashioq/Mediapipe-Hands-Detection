package com.jan.mediapipehandsdetection

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionDenied(
    permissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Camera permission is required",
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun HandTrackingContent(
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

        // Hand landmark overlay - always draw, use Box dimensions
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
                    // Settings button
                    IconButton(
                        onClick = { viewModel.toggleSettingsSheet() },
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }

                    // Camera switch button
                    IconButton(
                        onClick = { viewModel.toggleCamera() },
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    config: HandTrackingConfig,
    onDismiss: () -> Unit,
    onConfigUpdate: (HandTrackingConfig) -> Unit
) {
    var detectionConfidence by remember { mutableFloatStateOf(config.minHandDetectionConfidence) }
    var presenceConfidence by remember { mutableFloatStateOf(config.minHandPresenceConfidence) }
    var trackingConfidence by remember { mutableFloatStateOf(config.minTrackingConfidence) }
    var maxHands by remember { mutableIntStateOf(config.maxNumHands) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Hand Tracking Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Divider()

            // Detection Confidence
            Column {
                Text(
                    text = "Min Detection Confidence: ${String.format("%.2f", detectionConfidence)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = detectionConfidence,
                    onValueChange = { detectionConfidence = it },
                    valueRange = 0f..1f
                )
            }

            // Presence Confidence
            Column {
                Text(
                    text = "Min Presence Confidence: ${String.format("%.2f", presenceConfidence)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = presenceConfidence,
                    onValueChange = { presenceConfidence = it },
                    valueRange = 0f..1f
                )
            }

            // Tracking Confidence
            Column {
                Text(
                    text = "Min Tracking Confidence: ${String.format("%.2f", trackingConfidence)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = trackingConfidence,
                    onValueChange = { trackingConfidence = it },
                    valueRange = 0f..1f
                )
            }

            // Max Number of Hands
            Column {
                Text(
                    text = "Max Number of Hands: $maxHands",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = maxHands.toFloat(),
                    onValueChange = { maxHands = it.toInt() },
                    valueRange = 1f..2f,
                    steps = 0
                )
            }

            Divider()

            // Apply button
            Button(
                onClick = {
                    val newConfig = HandTrackingConfig(
                        minHandDetectionConfidence = detectionConfidence,
                        minHandPresenceConfidence = presenceConfidence,
                        minTrackingConfidence = trackingConfidence,
                        maxNumHands = maxHands
                    )
                    onConfigUpdate(newConfig)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Settings")
            }
        }
    }
}
