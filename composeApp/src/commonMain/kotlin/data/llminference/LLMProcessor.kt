package data.llminference

interface LLMProcessor {
    suspend fun generateText(photo: ByteArray, prompt: String): String
    suspend fun isModelAvailable(): Boolean
}