package ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Returns a composable that displays a camera icon.
 * This is implemented differently on each platform.
 */
@Composable
expect fun CameraIcon(modifier: Modifier = Modifier)