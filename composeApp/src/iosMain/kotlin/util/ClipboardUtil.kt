package util

import platform.Foundation.NSString
import platform.UIKit.UIPasteboard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS implementation of clipboard operations.
 */
actual fun copyToClipboard(text: String) {
    UIPasteboard.generalPasteboard.string = text
}

/**
 * iOS implementation of platform-specific clipboard utility.
 */
@Composable
actual fun rememberPlatformClipboardUtil(): ClipboardUtil {
    return remember { IosClipboardUtil() }
}

/**
 * iOS implementation of ClipboardUtil.
 */
private class IosClipboardUtil : ClipboardUtil {
    override fun copyToClipboard(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
