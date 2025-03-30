package data

import data.dto.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.ruggedodyssey.whatsfordinner.BuildKonfig

class VertexService {
    private val client = createHttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    suspend fun getGeminiResponse(encodedImage: String, prompt: String): Result<Recipe> {
        val builder = HttpRequestBuilder()

        builder.url("https://${API_ENDPOINT}/v1/projects/${PROJECT_ID}/locations/${LOCATION_ID}/publishers/google/models/${MODEL_ID}:streamGenerateContent")
        builder.method = HttpMethod.Post
        builder.headers {
            append("Content-Type", "application/json")
            append("Authorization", "Bearer ${BuildKonfig.TOKEN}")
        }
        val body = makeBody(prompt, encodedImage)
        builder.setBody(body)
        val response = client.post(builder)
        println(response.bodyAsText())
        return if (response.status.value in 200..299) {
            val result = json.decodeFromString<List<CandidatesResponse>>(response.bodyAsText())
            try {
                Result.success(formatResponse(result))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception(response.bodyAsText()))
        }
    }

    private fun makeBody(prompt: String, encodedImage: String): String {
        return json.encodeToString(
            GeminiPromptRequest(
                listOf(
                    Content(
                        role = "user",
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = InlineData(data = encodedImage))
                        )
                    )
                )
            )
        )
    }

    private fun formatResponse(result: List<CandidatesResponse>): Recipe {
        val responseText = result.joinToString("\n") { response ->
            response.candidates.joinToString("\n") { candidate ->
                candidate.content.parts.joinToString("\n") { part ->
                    part.text ?: ""
                }
            }
        }

        // Try to parse as Recipe using the extension function
        return try {
            responseText.toRecipe()
        } catch (e: Exception) {
            // If JSON parsing fails, return a basic Recipe with the response text as description
            Recipe(
                title = "Generated Recipe",
                description = responseText,
                ingredients = emptyList(),
                steps = emptyList()
            )
        }
    }

    private companion object {
        const val API_ENDPOINT = "us-central1-aiplatform.googleapis.com"
        const val PROJECT_ID = "whats-for-dinner-416112"
        const val LOCATION_ID = "europe-west3"
        const val MODEL_ID = "gemini-2.0-flash-001"
    }
}
