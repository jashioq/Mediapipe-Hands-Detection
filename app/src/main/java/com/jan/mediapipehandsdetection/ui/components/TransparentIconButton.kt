package com.jan.mediapipehandsdetection.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Icon button with semi transparent black background
 *
 * @param icon The icon to display
 * @param contentDescription Accessibility description
 * @param onClick Click handler
 * @param modifier Optional modifier
 */
@Composable
fun TransparentIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.background(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}
