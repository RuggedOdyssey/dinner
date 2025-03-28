package domain

import data.dto.Output
import data.dto.Recipe

class GetMockGeminiRecipeUseCase : RecipeUseCase {

    override suspend operator fun invoke(
        photo: ByteArray, 
        availableProducts: String, 
        recipeTitle: String?,
        dietaryPreferences: DietaryPreferences
    ): Result<Output> {
        // Return a mock response
        return Result.success(createMockOutput(availableProducts, recipeTitle, dietaryPreferences))
    }

    private fun createMockOutput(availableProducts: String, recipeTitle: String?, dietaryPreferences: DietaryPreferences): Output {
        val ingredients = availableProducts.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // Add a note about dietary preferences in the description
        val dietaryNote = if (dietaryPreferences != DietaryPreferences()) {
            " Respects ${dietaryPreferences.toString()}."
        } else {
            ""
        }

        return Output(
            groceries = listOf("250g flour", "2 eggs", "500ml milk", "1 tsp salt", "2 tbsp olive oil"),
            recipe = Recipe(
                title = recipeTitle ?: "Mock Recipe with ${ingredients.joinToString(", ")}",
                description = "This is a mock recipe generated in offline mode.$dietaryNote",
                ingredients = ingredients + listOf("250g flour", "2 eggs", "500ml milk", "1 tsp salt", "2 tbsp olive oil"),
                steps = listOf(
                    "Mix all ingredients in a bowl.",
                    "Cook on medium heat for 10 minutes.",
                    "Serve hot and enjoy your meal!"
                )
            )
        )
    }
}
