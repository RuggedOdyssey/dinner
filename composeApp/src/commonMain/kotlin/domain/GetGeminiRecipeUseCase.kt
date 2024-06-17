package domain

import data.VertexService
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class GetGeminiRecipeUseCase(private val service: VertexService = VertexService()) {

    suspend operator fun invoke(photo: ByteArray, availableProducts: String): Result<String> {
        val encodedBitmap = Base64.encode(photo)
        val prompt = makePrompt(availableProducts)
        return service.getGeminiResponse(encodedBitmap, prompt)
    }

    private fun makePrompt(availableProducts: String) =
        PROMPT_WRAPPER_PREFIX + availableProducts + PROMPT_WRAPPER_SUFFIX

    private companion object {
        const val PROMPT_WRAPPER_PREFIX =
            "Given the image of an italian takeaway dish, write a recipe and include the following ingredients:\n"
        const val PROMPT_WRAPPER_SUFFIX =
            "\nThen provide grocery list of the additional ingredients with there quantities in metric, which is needed for the recipe. exclude the ingredients I listed already."
    }
}