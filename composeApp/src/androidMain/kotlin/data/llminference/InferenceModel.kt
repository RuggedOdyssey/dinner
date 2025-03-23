package data.llminference

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resumeWithException

/** The maximum number of tokens the model can process. */
var MAX_TOKENS = 1024

/**
 * An offset in tokens that we use to ensure that the model always has the
 * ability to respond when we compute the remaining context length.
 */
//var DECODE_TOKEN_OFFSET = 256

class ModelLoadFailException :
    Exception("Failed to load model, please try again")

class ModelSessionCreateFailException :
    Exception("Failed to create model session, please try again")

class ModelPromptFailException :
    Exception("Failed to infer from prompt, please try again")

class InferenceModel(private val context: Context) : LLMProcessor {
    private lateinit var llmInference: LlmInference
    private lateinit var llmInferenceSession: LlmInferenceSession
    private val TAG = InferenceModel::class.qualifiedName

    // val uiState: UiState

    init {
        if (!modelExists(context)) {
            throw IllegalArgumentException("Model not found at path: ${model.path}")
        }

        // uiState = model.uiState
        createEngine(context)
        createSession()
    }

    fun close() {
        llmInferenceSession.close()
        llmInference.close()
    }

    fun resetSession() {
        llmInferenceSession.close()
        createSession()
    }

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
            throw ModelLoadFailException()
        }
    }

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
            throw ModelSessionCreateFailException()
        }
    }

    override suspend fun generateText(photo: ByteArray, prompt: String): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                // Add query to the session
                llmInferenceSession.addQueryChunk(prompt)

                // StringBuilder to accumulate partial results
                val resultBuilder = StringBuilder()

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
                                    continuation.resumeWithException(ModelPromptFailException())
                                }
                            }
                        } catch (e: Exception) {
                            // Handle any errors during result processing
                            continuation.resumeWithException(e)
                        }
                    }
                })
            } catch (e: Exception) {
                // Handle any setup or immediate failure
                continuation.resumeWithException(e)
            }
        }
    }

    override suspend fun isModelAvailable(): Boolean = modelExists(context)

    companion object {
        var model: Model = Model.GEMMA3_CPU
//        private var instance: InferenceModel? = null
//
//        fun getInstance(context: Context): InferenceModel {
//            return if (instance != null) {
//                instance!!
//            } else {
//                InferenceModel(context).also { instance = it }
//            }
//        }
//
//        fun resetInstance(context: Context): InferenceModel {
//            return InferenceModel(context).also { instance = it }
//        }

        fun modelPathFromUrl(context: Context): String {
            if (model.url.isNotEmpty()) {
                val urlFileName = Uri.parse(model.url).lastPathSegment
                if (!urlFileName.isNullOrEmpty()) {
                    return File(context.filesDir, urlFileName).absolutePath
                }
            }

            return ""
        }

        fun modelPath(context: Context): String {
            val modelFile = File(model.path)
            if (modelFile.exists()) {
                return model.path
            }

            return modelPathFromUrl(context)
        }

        fun modelExists(context: Context): Boolean {
            return File(modelPath(context)).exists()
        }
    }
}
