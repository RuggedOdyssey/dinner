package permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.ruggedodyssey.whatsfordinner.MainActivity

/**
 * Android implementation of CameraPermissionHandler
 */
class AndroidCameraPermissionHandler(private val context: Context) : CameraPermissionHandler {

    private val _cameraPermissionState = MutableStateFlow(isCameraPermissionGranted())
    override val cameraPermissionState: StateFlow<Boolean> = _cameraPermissionState.asStateFlow()

    override fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestCameraPermission() {
        if (!isCameraPermissionGranted()) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            _cameraPermissionState.value = true
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}

/**
 * Create Android-specific implementation of CameraPermissionHandler
 */
actual fun createCameraPermissionHandler(): CameraPermissionHandler {
    // Get the application context from MainActivity
    return AndroidCameraPermissionHandler(MainActivity.getInstance())
}
