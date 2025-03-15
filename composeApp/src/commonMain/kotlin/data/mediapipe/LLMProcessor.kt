package data.mediapipe

interface LLMProcessor {
    suspend fun generateText(prompt: String): String
    suspend fun isModelAvailable(): Boolean
    suspend fun downloadModelIfNeeded()
}