package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CandidatesResponse(
    val candidates: List<Candidate>
)