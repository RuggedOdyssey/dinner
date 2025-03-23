package data.llminference

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
//import com.google.mediapipe.framework.image.MPImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Closeable
import kotlin.coroutines.resumeWithException

/**
 * Exception thrown when the model fails to load.
 *
 * @param message The error message
 * @param cause The underlying cause of the exception
 */
class ModelLoadFailException(message: String = "Failed to load model", cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Exception thrown when the model session fails to create.
 *
 * @param message The error message
 * @param cause The underlying cause of the exception
 */
class ModelSessionCreateFailException(message: String = "Failed to create model session", cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Exception thrown when the model fails to process a prompt.
 *
 * @param message The error message
 * @param cause The underlying cause of the exception
 */
class ModelPromptFailException(message: String = "Failed to infer from prompt", cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Implementation of [LLMProcessor] that uses MediaPipe's LLM inference for text generation.
 *
 * This class handles loading a model, creating an inference session, and generating text
 * based on a photo and a prompt.
 *
 * @property context The Android context used for model loading and inference
 */
class InferenceModel(private val context: Context) : LLMProcessor, Closeable {
    private lateinit var llmInference: LlmInference
    private lateinit var llmInferenceSession: LlmInferenceSession
    private val TAG = InferenceModel::class.qualifiedName

    // Cache for model availability to avoid redundant checks
    private var isModelAvailableCache: Boolean? = null

    init {
        try {
            if (!modelExists(context)) {
                isModelAvailableCache = false
                throw IllegalArgumentException("Model not found at path: ${model.path}")
            }

            isModelAvailableCache = true
            createEngine(context)
            createSession()
        } catch (e: Exception) {
            Log.e(TAG, "Initialization error: ${e.message}", e)
            isModelAvailableCache = false
            throw e
        }
    }

    /**
     * Closes the inference session and engine, releasing all resources.
     * This method should be called when the model is no longer needed.
     */
    override fun close() {
        try {
            if (::llmInferenceSession.isInitialized) {
                llmInferenceSession.close()
            }
            if (::llmInference.isInitialized) {
                llmInference.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing resources: ${e.message}", e)
        }
    }

    /**
     * Resets the inference session, creating a new one with the current model configuration.
     * This can be useful when you want to start a new conversation or clear the session state.
     */
    fun resetSession() {
        try {
            if (::llmInferenceSession.isInitialized) {
                llmInferenceSession.close()
            }
            createSession()
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting session: ${e.message}", e)
            throw ModelSessionCreateFailException("Failed to reset session", e)
        }
    }

    /**
     * Creates the inference engine with the specified model and options.
     *
     * @param context The Android context
     * @throws ModelLoadFailException if the model fails to load
     */
    private fun createEngine(context: Context) {
        val inferenceOptions = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath(context))
            .setMaxTokens(MAX_TOKENS)
            .apply { model.preferredBackend?.let { setPreferredBackend(it) } }
            .build()

        try {
            llmInference = LlmInference.createFromOptions(context, inferenceOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Load model error: ${e.message}", e)
            throw ModelLoadFailException("Failed to load model: ${e.message}", e)
        }
    }

    /**
     * Creates the inference session with the specified options.
     *
     * @throws ModelSessionCreateFailException if the session fails to create
     */
    private fun createSession() {
        val sessionOptions = LlmInferenceSessionOptions.builder()
            .setTemperature(model.temperature)
            .setTopK(model.topK)
            .setTopP(model.topP)
            .build()

        try {
            llmInferenceSession =
                LlmInferenceSession.createFromOptions(llmInference, sessionOptions)
        } catch (e: Exception) {
            Log.e(TAG, "LlmInferenceSession create error: ${e.message}", e)
            throw ModelSessionCreateFailException("Failed to create model session: ${e.message}", e)
        }
    }

    /**
     * Generates text based on a photo and a prompt.
     *
     * @param photo The photo to analyze (currently not used in this implementation)
     * @param prompt The prompt to use for text generation
     * @return The generated text
     * @throws ModelPromptFailException if the model fails to process the prompt
     */
    override suspend fun generateText(photo: ByteArray, prompt: String): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                // Note: This implementation currently doesn't use the photo parameter
                // Future implementations could incorporate the photo into the prompt or use multimodal models

                resetSession()
                // Add query to the session
                llmInferenceSession.addQueryChunk(prompt)
               // val mpImage = MPImage.createFromByteArray(photo)
               // llmInferenceSession.addImage(mpImage)

                // StringBuilder to accumulate partial results
                val resultBuilder = StringBuilder()

                // Set up cancellation handler
                continuation.invokeOnCancellation {
                    try {
                        // Clean up resources if the coroutine is cancelled
                        resetSession()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during cancellation: ${e.message}", e)
                    }
                }

                // Call the API and register a listener
                llmInferenceSession.generateResponseAsync(object : ProgressListener<String> {
                    override fun run(partialResult: String?, done: Boolean) {
                        try {
                            // Add partial result to the accumulated result if not null
                            if (partialResult != null) {
                                resultBuilder.append(partialResult)
                            }

                            // Only resume the continuation when the response is complete
                            if (done) {
                                val completeResult = resultBuilder.toString()
                                continuation.resume(completeResult) { cause, _, _ ->
                                    continuation.resumeWithException(
                                        ModelPromptFailException("Failed to complete text generation", cause)
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // Handle any errors during result processing
                            Log.e(TAG, "Error processing partial result: ${e.message}", e)
                            continuation.resumeWithException(
                                ModelPromptFailException("Error processing partial result: ${e.message}", e)
                            )
                        }
                    }
                })
            } catch (e: Exception) {
                // Handle any setup or immediate failure
                Log.e(TAG, "Error generating text: ${e.message}", e)
                continuation.resumeWithException(
                    ModelPromptFailException("Error generating text: ${e.message}", e)
                )
            }
        }
    }

    /**
     * Checks if the model is available.
     *
     * @return True if the model is available, false otherwise
     */
    override suspend fun isModelAvailable(): Boolean {
        // Use cached value if available
        isModelAvailableCache?.let { return it }

        // Otherwise check the file system and cache the result
        val available = modelExists(context)
        isModelAvailableCache = available
        return available
    }

    companion object {
        // Maximum number of tokens the model can process
        private const val MAX_TOKENS = 1024

        // Default model configuration
        @Volatile
        private var _model: Model = Model.GEMMA3_CPU //Model.GEMMA2_CPU

        /**
         * Gets the current model configuration.
         */
        val model: Model
            get() = _model

        /**
         * Sets the model configuration to use for inference.
         * This method ensures thread-safe updates to the model configuration.
         *
         * @param newModel The new model configuration to use
         */
        fun setModel(newModel: Model) {
            synchronized(this) {
                _model = newModel
            }
        }

        /**
         * Gets the path to the model file from a URL.
         *
         * @param context The Android context
         * @return The path to the model file, or an empty string if the model URL is invalid
         */
        fun modelPathFromUrl(context: Context): String {
            if (model.url.isNotEmpty()) {
                val urlFileName = Uri.parse(model.url).lastPathSegment
                if (!urlFileName.isNullOrEmpty()) {
                    return File(context.filesDir, urlFileName).absolutePath
                }
            }

            return ""
        }

        /**
         * Gets the path to the model file.
         * First checks if the model exists at the specified path, then falls back to the URL-based path.
         *
         * @param context The Android context
         * @return The path to the model file
         */
        fun modelPath(context: Context): String {
            val modelFile = File(model.path)
            if (modelFile.exists()) {
                return model.path
            }

            return modelPathFromUrl(context)
        }

        /**
         * Checks if the model file exists.
         *
         * @param context The Android context
         * @return True if the model file exists, false otherwise
         */
        fun modelExists(context: Context): Boolean {
            return File(modelPath(context)).exists()
        }
    }
}
