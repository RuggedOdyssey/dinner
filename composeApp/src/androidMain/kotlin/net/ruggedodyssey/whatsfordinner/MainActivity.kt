package net.ruggedodyssey.whatsfordinner

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import permissions.createCameraPermissionHandler
import ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Store the instance for access by other components
        instance = this

        // Request camera permission on startup if not already granted
        val cameraPermissionHandler = createCameraPermissionHandler()
        if (!cameraPermissionHandler.isCameraPermissionGranted()) {
            cameraPermissionHandler.requestCameraPermission()
        }

        setContent {
            MainScreen()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    companion object {
        private lateinit var instance: MainActivity

        fun getInstance(): MainActivity {
            return instance
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MainScreen()
}
