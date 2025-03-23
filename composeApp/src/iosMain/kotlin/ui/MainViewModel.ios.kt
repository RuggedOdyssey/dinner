package ui

import data.llminference.LLMFactory

actual fun createLLMFactory(): LLMFactory {
    return LLMFactory()
}
