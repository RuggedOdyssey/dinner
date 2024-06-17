package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class InlineData(val mimeType: String = "image/jpeg", val data: String?)