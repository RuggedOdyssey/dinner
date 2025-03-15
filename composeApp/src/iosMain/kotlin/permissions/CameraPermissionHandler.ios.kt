package permissions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSLog

/**
 * iOS implementation of CameraPermissionHandler
 */
class IOSCameraPermissionHandler : CameraPermissionHandler {
    
    private val _cameraPermissionState = MutableStateFlow(isCameraPermissionGranted())
    override val cameraPermissionState: StateFlow<Boolean> = _cameraPermissionState.asStateFlow()
    
    override fun isCameraPermissionGranted(): Boolean {
        val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return authStatus == AVAuthorizationStatusAuthorized
    }
    
    override fun requestCameraPermission() {
        val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        
        when (authStatus) {
            AVAuthorizationStatusAuthorized -> {
                // Permission already granted
                _cameraPermissionState.value = true
                NSLog("Camera permission already granted")
            }
            AVAuthorizationStatusNotDetermined -> {
                // Permission not determined, request it
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    _cameraPermissionState.value = granted
                    NSLog("Camera permission request result: $granted")
                }
            }
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                // Permission denied or restricted
                _cameraPermissionState.value = false
                NSLog("Camera permission denied or restricted")
            }
            else -> {
                // Unknown status
                _cameraPermissionState.value = false
                NSLog("Unknown camera permission status")
            }
        }
    }
}

/**
 * Create iOS-specific implementation of CameraPermissionHandler
 */
actual fun createCameraPermissionHandler(): CameraPermissionHandler {
    return IOSCameraPermissionHandler()
}