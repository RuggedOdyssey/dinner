package ui

import android.app.Application
import data.mediapipe.LLMFactory
import net.ruggedodyssey.whatsfordinner.MainActivity

actual fun createLLMFactory(): LLMFactory {
    // This assumes that MainActivity will expose the application context
    return LLMFactory(MainActivity.getInstance().applicationContext)
}