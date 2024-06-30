package domain

import data.VertexService
import data.dto.Output
import data.dto.outputFormat
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class GetGeminiRecipeUseCase(private val service: VertexService = VertexService()) {

    suspend operator fun invoke(photo: ByteArray, availableProducts: String): Result<Output> {
        val encodedBitmap = Base64.encode(photo)
        val prompt = makePrompt(availableProducts)
        return service.getGeminiResponse(encodedBitmap, prompt)
    }

    private fun makePrompt(availableProducts: String) =
        PROMPT_WRAPPER_PREFIX + availableProducts + PROMPT_WRAPPER_SUFFIX + FORMAT_REQUEST + outputFormat

    private companion object {
        const val PROMPT_WRAPPER_PREFIX =
            "Given the image of a takeaway dish, write a recipe that can easily be made at home and include the following ingredients:\n"
        const val PROMPT_WRAPPER_SUFFIX =
            "\nThen provide grocery list of the additional ingredients with there quantities in metric, which is needed for the recipe. exclude the ingredients I listed already."
        const val FORMAT_REQUEST = "\nformat the response in a JSON object in the following format\n Do not output markdown\n"
    }
}