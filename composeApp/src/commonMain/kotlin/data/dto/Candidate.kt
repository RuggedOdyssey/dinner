package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class Candidate(
    val content: Content
)