package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)