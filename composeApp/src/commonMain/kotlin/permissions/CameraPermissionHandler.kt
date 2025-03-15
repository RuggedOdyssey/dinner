package permissions

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for handling camera permissions across platforms
 */
interface CameraPermissionHandler {
    /**
     * Check if camera permission is granted
     * @return true if permission is granted, false otherwise
     */
    fun isCameraPermissionGranted(): Boolean
    
    /**
     * Request camera permission
     * This should be called on app startup to ensure camera permission is requested early
     */
    fun requestCameraPermission()
    
    /**
     * StateFlow that emits the current permission state
     * This can be observed to react to permission changes
     */
    val cameraPermissionState: StateFlow<Boolean>
}

/**
 * Create platform-specific implementation of CameraPermissionHandler
 */
expect fun createCameraPermissionHandler(): CameraPermissionHandler