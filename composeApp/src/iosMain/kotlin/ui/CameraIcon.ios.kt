package ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS implementation of CameraIcon that uses an emoji as a fallback.
 */
@Composable
actual fun CameraIcon(modifier: Modifier) {
    Text("📷", modifier = modifier)
}