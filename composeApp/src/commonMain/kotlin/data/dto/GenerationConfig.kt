package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerationConfig(val maxOutputTokens: Int = 8192, val temperature: Double = 1.0, val topP: Double = 0.95)