package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    val role: String = "model",
    val parts: List<Part> = emptyList(),
)