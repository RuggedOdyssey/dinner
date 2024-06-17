package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SafetySetting(
    val category: String,
    val threshold: String
)