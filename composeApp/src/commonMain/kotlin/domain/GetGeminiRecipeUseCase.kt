package domain

import data.VertexService
import data.dto.Output
import data.dto.Recipe
import data.dto.outputFormat
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class GetGeminiRecipeUseCase(private val service: VertexService = VertexService()) {

    suspend operator fun invoke(photo: ByteArray, availableProducts: String, isOfflineMode: Boolean = false): Result<Output> {
        return if (isOfflineMode) {
            // Return a mock response in offline mode
            Result.success(createMockOutput(availableProducts))
        } else {
            // Use the online service in online mode
            val encodedBitmap = Base64.encode(photo)
            val prompt = makePrompt(availableProducts)
            service.getGeminiResponse(encodedBitmap, prompt)
        }
    }

    private fun createMockOutput(availableProducts: String): Output {
        val ingredients = availableProducts.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return Output(
            groceries = listOf("250g flour", "2 eggs", "500ml milk", "1 tsp salt", "2 tbsp olive oil"),
            recipe = Recipe(
                title = "Mock Recipe with ${ingredients.joinToString(", ")}",
                description = "This is a mock recipe generated in offline mode.",
                ingredients = ingredients + listOf("250g flour", "2 eggs", "500ml milk", "1 tsp salt", "2 tbsp olive oil"),
                steps = listOf(
                    "Mix all ingredients in a bowl.",
                    "Cook on medium heat for 10 minutes.",
                    "Serve hot and enjoy your meal!"
                )
            )
        )
    }

    private fun makePrompt(availableProducts: String) =
        PROMPT_WRAPPER_PREFIX + availableProducts + PROMPT_WRAPPER_SUFFIX + FORMAT_REQUEST + outputFormat

    private companion object {
        const val PROMPT_WRAPPER_PREFIX =
            "Given the image of a takeaway dish, write a recipe that can easily be made at home and include the following ingredients:\n"
        const val PROMPT_WRAPPER_SUFFIX =
            "\nThen provide grocery list of the additional ingredients with there quantities in metric, which is needed for the recipe. exclude the ingredients I listed already."
        const val FORMAT_REQUEST = "\nformat the response in a valid JSON object with all brackets matching in the following format\n Do not output markdown\n"
    }
}
