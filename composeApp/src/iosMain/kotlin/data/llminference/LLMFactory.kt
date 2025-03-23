package data.llminference

actual class LLMFactory {
    actual fun createLLMProcessor(): LLMProcessor {
        return InferenceModel()
    }
}