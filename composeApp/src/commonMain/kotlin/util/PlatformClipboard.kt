package util

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable to get a clipboard utility.
 */
@Composable
expect fun rememberPlatformClipboardUtil(): ClipboardUtil

/**
 * Interface for clipboard operations.
 */
interface ClipboardUtil {
    fun copyToClipboard(text: String)
}