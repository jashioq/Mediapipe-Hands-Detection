package com.jan.mediapipehandsdetection.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jan.mediapipehandsdetection.models.HandTrackingConfig

/**
 * Bottom sheet for adjusting hand tracking settings
 *
 * @param config Current hand tracking configuration
 * @param onDismiss Callback when sheet is dismissed
 * @param onConfigUpdate Callback when new configuration is applied
 */
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
