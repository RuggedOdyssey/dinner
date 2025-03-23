package data.llminference

import android.content.Context

actual class LLMFactory(private val context: Context) {
    actual fun createLLMProcessor(): LLMProcessor {
        return InferenceModel(context)
    }
}