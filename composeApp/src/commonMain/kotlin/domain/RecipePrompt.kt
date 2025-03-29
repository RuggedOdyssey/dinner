package domain

object RecipePrompt {
    const val PROMPT_WRAPPER_PREFIX =
        "Given the image of a takeaway dish, write a recipe that can easily be made at home and include the following ingredients:\n"
    const val PROMPT_WRAPPER_SUFFIX =
        "\nThen provide grocery list of the additional ingredients with there quantities in metric, which is needed for the recipe. exclude the ingredients I listed already."

    fun makePrompt(availableProducts: String, recipeTitle: String? = null, dietaryPreferences: DietaryPreferences = DietaryPreferences()): String {
        val titleInstruction = if (!recipeTitle.isNullOrBlank()) {
            "\nThe recipe title should be: $recipeTitle"
        } else {
            ""
        }
        return PROMPT_WRAPPER_PREFIX + availableProducts + titleInstruction + "\n" + dietaryPreferences.toString() + PROMPT_WRAPPER_SUFFIX //+ FORMAT_REQUEST
    }
}
