package domain

object RecipePrompt {
    const val PROMPT_WRAPPER_PREFIX =
        "You are a hungry home cook. Given the image of a takeaway dish, write a recipe that can easily be made at home and include the following ingredients which I have in my pantry:\n"
    const val PROMPT_WRAPPER_SUFFIX =
        "\nThen provide grocery list of the additional ingredients needed for the recipe. Give the quantities for the ingredients in metric. Exclude the pantry ingredients already listed from the grocery list."

    fun makePrompt(availableProducts: String, recipeTitle: String? = null, dietaryPreferences: DietaryPreferences = DietaryPreferences()): String {
        val titleInstruction = if (!recipeTitle.isNullOrBlank()) {
            "\nThe recipe title should be: $recipeTitle"
        } else {
            ""
        }
        return "${PROMPT_WRAPPER_PREFIX}${availableProducts}${titleInstruction}\n${dietaryPreferences}${PROMPT_WRAPPER_SUFFIX}"
    }
}
