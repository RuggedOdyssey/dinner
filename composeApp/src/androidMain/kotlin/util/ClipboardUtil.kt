package util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Android implementation of clipboard operations.
 */
actual fun copyToClipboard(text: String) {
    // This function needs a context, which is not available here
    // We'll provide a composable function that can be used in UI code
}

/**
 * Android implementation of platform-specific clipboard utility.
 */
@Composable
actual fun rememberPlatformClipboardUtil(): ClipboardUtil {
    val context = LocalContext.current
    return remember { ClipboardUtilImpl(context) }
}

/**
 * Android implementation of ClipboardUtil.
 */
private class ClipboardUtilImpl(private val context: Context) : ClipboardUtil {
    override fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Recipe Title", text)
        clipboard.setPrimaryClip(clip)
    }
}
