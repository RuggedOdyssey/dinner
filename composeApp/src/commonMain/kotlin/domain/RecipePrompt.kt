package domain

import data.dto.outputFormat

object RecipePrompt {
    const val PROMPT_WRAPPER_PREFIX =
        "Given the image of a takeaway dish, write a recipe that can easily be made at home and include the following ingredients:\n"
    const val PROMPT_WRAPPER_SUFFIX =
        "\nThen provide grocery list of the additional ingredients with there quantities in metric, which is needed for the recipe. exclude the ingredients I listed already."
    const val FORMAT_REQUEST = "\nformat the response in a valid JSON object with all brackets matching in the following format\n Do not output markdown\n"

    fun makePrompt(availableProducts: String, recipeTitle: String? = null): String {
        val titleInstruction = if (!recipeTitle.isNullOrBlank()) {
            "\nThe recipe title should be: $recipeTitle"
        } else {
            ""
        }
        return PROMPT_WRAPPER_PREFIX + availableProducts + titleInstruction + PROMPT_WRAPPER_SUFFIX + FORMAT_REQUEST + outputFormat
    }
}
