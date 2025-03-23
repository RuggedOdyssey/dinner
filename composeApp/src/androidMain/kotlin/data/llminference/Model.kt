package data.llminference

import com.google.mediapipe.tasks.genai.llminference.LlmInference.Backend

// NB: Make sure the filename is *unique* per model you use!
// Weight caching is currently based on filename alone.
enum class Model(
    val path: String,
    val url: String,
    val preferredBackend: Backend?,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
) {
    GEMMA3_CPU(
        path = "/data/local/tmp/gemma3-1b-it-int4.task",
        url = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task",
        preferredBackend = Backend.CPU,
        temperature = 1f,
        topK = 64,
        topP = 0.95f
    ),

    GEMMA2_CPU(
        path = "/data/local/tmp/gemma2_q8_multi-prefill-seq_ekv1280.task",
        url = "https://huggingface.co/litert-community/Gemma2-2B-IT/resolve/main/gemma2_q8_multi-prefill-seq_ekv1280.task",
        preferredBackend = Backend.CPU,
        temperature = 1f,
        topK = 64,
        topP = 0.95f
    ),
    GEMMA3_GPU(
        path = "/data/local/tmp/gemma3-1b-it-int4.task",
        url = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task",
        preferredBackend = Backend.GPU,
        temperature = 1f,
        topK = 64,
        topP = 0.95f
    ),
//    DEEPSEEK_CPU(
//        path = "/data/local/tmp/llm/deepseek3k_q8_ekv1280.task",
//        url = "https://huggingface.co/litert-community/DeepSeek-R1-Distill-Qwen-1.5B/resolve/main/deepseek_q8_ekv1280.task",
//        licenseUrl = "",
//        needsAuth = false,
//        preferredBackend = null,
//       // uiState = DeepSeekUiState(),
//        temperature = 0.6f,
//        topK = 40,
//        topP = 0.7f
//    ),
    PHI4_CPU(
        path = "/data/local/tmp/phi4_q8_ekv1280.task",
        url = "https://huggingface.co/litert-community/Phi-4-mini-instruct/resolve/main/phi4_q8_ekv1280.task",
        preferredBackend = null,
        temperature = 0.0f,
        topK = 40,
        topP = 1.0f
    ),
}
