package ui

import data.mediapipe.LLMFactory

actual fun createLLMFactory(): LLMFactory {
    return LLMFactory()
}