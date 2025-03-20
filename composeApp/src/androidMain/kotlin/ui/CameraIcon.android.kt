package ui

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import net.ruggedodyssey.whatsfordinner.R

/**
 * Android implementation of CameraIcon that uses the baseline_camera vector drawable.
 */
@Composable
actual fun CameraIcon(modifier: Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.baseline_camera_alt_24),
        contentDescription = "Camera",
        modifier = modifier
    )
}