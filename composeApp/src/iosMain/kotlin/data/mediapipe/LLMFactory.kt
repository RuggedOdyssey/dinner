package data.mediapipe

actual class LLMFactory {
    actual fun createLLMProcessor(): LLMProcessor {
        return MediaPipeProcessor()
    }
}