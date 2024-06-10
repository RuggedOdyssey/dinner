import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class MainViewModel : ViewModel() {

    val state = MutableStateFlow<MainViewState>(MainViewState.Input)

    fun getRecipe(image: ByteArray, input: MutableState<String>) = viewModelScope.launch(Dispatchers.IO) {
        state.value = MainViewState.Loading
        val client = httpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                }) // Example: Register JSON content transformation
                // Add more transformations as needed for other content types
            }
        }
        val encodedBitmap = Base64.encode(image)
        val builder = HttpRequestBuilder()

        builder.url("https://${API_ENDPOINT}/v1/projects/${PROJECT_ID}/locations/${LOCATION_ID}/publishers/google/models/${MODEL_ID}:streamGenerateContent")
        builder.method = HttpMethod.Post
        builder.headers {
            append("Content-Type", "application/json")
            append("Authorization", "Bearer $TOKEN")
        }
        val body = Json {
            encodeDefaults = true
            explicitNulls = false
        }.encodeToString(
            GeminiPromptRequest(
                listOf(
                    Content(
                        role = "user",
                        parts = listOf(
                            Part(
                                text = makePrompt(input)
                            ),
                            Part(
                                inlineData = InlineData(data = encodedBitmap)
                            )
                        )
                    )
                )
            )
        )
        builder.setBody(
            body
        )
        val response = client.post(builder)
        println(response.bodyAsText())
        if (response.status.value in 200..299) {
            val result =
                Json { ignoreUnknownKeys = true }.decodeFromString<List<CandidatesResponse>>(response.bodyAsText())
            // TODO fix formatting for different parts of responses
            val responseText = result.joinToString("\n") { response ->
                response.candidates.joinToString("\n") { candidate ->
                    candidate.content.parts.joinToString("\n") { part ->
                        part.text ?: ""
                    }
                }
            }
            state.value = MainViewState.Success(responseText)
        } else {
            state.value = MainViewState.Error(response.body())
        }
    }

    private fun makePrompt(input: MutableState<String>) = PROMPT_WRAPPER_PREFIX + input.value + PROMPT_WRAPPER_SUFFIX

    companion object {
        const val API_ENDPOINT = "us-central1-aiplatform.googleapis.com"
        const val PROJECT_ID = "whats-for-dinner-416112"
        const val LOCATION_ID = "us-central1"
        const val MODEL_ID = "gemini-1.5-flash-001"
        const val TOKEN =
            "ya29.a0AXooCguZ3Fw4JCQQ4b58sDEnE4FWsp4GWNcFUEb7kHu2dXdpS3bA9LN7JTzlsLNsf9mmoASePJr01BTl28akaKGxeioqb5tBL6Hk5G6QtpjwJ9oX0Xh1PmGprTvKWiEJ-JeAuUNjAOG7tyEjraYmTSvXUt1GD-T5JXHZFjn2NtEaCgYKAYISARMSFQHGX2MiDyDJeLk0paaR1ufnbsvdQA0178"

        const val PROMPT_WRAPPER_PREFIX =
            "Given the image of an italian takeaway dish, write a recipe and include the following ingredients:\n"
        const val PROMPT_WRAPPER_SUFFIX =
            "\nThen provide grocery list of the additional ingredients with there quantities in metric, which is needed for the recipe. exclude the ingredients I listed already."
    }

    @Serializable
    data class GeminiPromptRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig = GenerationConfig(),
        val safetySettings: List<SafetySetting> = listOf(
            SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_MEDIUM_AND_ABOVE"),
            SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE"),
            SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"),
            SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_MEDIUM_AND_ABOVE"),
        )
    )

    @Serializable
    data class GenerationConfig(val maxOutputTokens: Int = 8192, val temperature: Double = 1.0, val topP: Double = 0.95)

    @Serializable
    data class Content(
        val role: String = "model",
        val parts: List<Part> = emptyList(),
    )

    @Serializable
    data class Part(
        val text: String? = null,
        val inlineData: InlineData? = null
    )

    @Serializable
    data class InlineData(val mimeType: String = "image/jpeg", val data: String?)

    @Serializable
    data class SafetySetting(
        val category: String,
        val threshold: String
    )

    @Serializable
    data class CandidatesResponse(
        val candidates: List<Candidate>
    )

    @Serializable
    data class Candidate(
        val content: Content
    )

    sealed interface MainViewState {
        data object Input : MainViewState
        data object Loading : MainViewState
        data class Success(val result: String) : MainViewState
        data class Error(val text: String) : MainViewState
    }
}

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient