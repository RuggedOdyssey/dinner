package data

import net.ruggedodyssey.whatsfordinner.BuildKonfig
import data.dto.CandidatesResponse
import data.dto.Content
import data.dto.GeminiPromptRequest
import data.dto.InlineData
import data.dto.Output
import data.dto.Part
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

    suspend fun getGeminiResponse(encodedImage: String, prompt: String): Result<Output> {
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
            } catch (e:Exception) {
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

    private fun String.toOutput() = Json{ ignoreUnknownKeys = true }.decodeFromString<Output>(this)
    private fun formatResponse(result: List<CandidatesResponse>) = result.joinToString("\n") { response ->
        response.candidates.joinToString("\n") { candidate ->
            candidate.content.parts.joinToString("\n") { part ->
                part.text ?: ""
            }
        }
    }.toOutput()

    private companion object {
        const val API_ENDPOINT = "us-central1-aiplatform.googleapis.com"
        const val PROJECT_ID = "whats-for-dinner-416112"
        const val LOCATION_ID = "europe-west3"
        const val MODEL_ID = "gemini-1.5-flash-001"
    }
}