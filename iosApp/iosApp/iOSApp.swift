import SwiftUI
import AVFoundation

@main
struct iOSApp: App {
    init() {
        // Request camera permission on startup
        requestCameraPermission()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func requestCameraPermission() {
        let authStatus = AVCaptureDevice.authorizationStatus(for: .video)

        switch authStatus {
        case .notDetermined:
            // Permission not determined, request it
            AVCaptureDevice.requestAccess(for: .video) { granted in
                print("Camera permission request result: \(granted)")
            }
        case .authorized:
            // Permission already granted
            print("Camera permission already granted")
        case .denied, .restricted:
            // Permission denied or restricted
            print("Camera permission denied or restricted")
        @unknown default:
            // Unknown status
            print("Unknown camera permission status")
        }
    }
}
