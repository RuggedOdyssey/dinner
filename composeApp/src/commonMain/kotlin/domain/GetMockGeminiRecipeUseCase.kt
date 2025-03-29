package domain

import data.dto.Output
import data.dto.Recipe
import data.dto.Ingredient

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
            groceries = listOf(
                Ingredient(name = "flour", quantity = "250g"),
                Ingredient(name = "eggs", quantity = "2"),
                Ingredient(name = "milk", quantity = "500ml"),
                Ingredient(name = "salt", quantity = "1 tsp"),
                Ingredient(name = "olive oil", quantity = "2 tbsp")
            ),
            recipe = Recipe(
                title = recipeTitle ?: "Mock Recipe with ${ingredients.joinToString(", ")}",
                description = "This is a mock recipe generated in offline mode.$dietaryNote",
                ingredients = ingredients.map { ingredient ->
                    // Try to split the ingredient into quantity and name
                    val parts = ingredient.split(" ", limit = 2)
                    if (parts.size > 1) {
                        Ingredient(name = parts[1], quantity = parts[0])
                    } else {
                        Ingredient(name = ingredient, quantity = "")
                    }
                } + listOf(
                    Ingredient(name = "flour", quantity = "250g"),
                    Ingredient(name = "eggs", quantity = "2"),
                    Ingredient(name = "milk", quantity = "500ml"),
                    Ingredient(name = "salt", quantity = "1 tsp"),
                    Ingredient(name = "olive oil", quantity = "2 tbsp")
                ),
                steps = listOf(
                    "Mix all ingredients in a bowl.",
                    "Cook on medium heat for 10 minutes.",
                    "Serve hot and enjoy your meal!"
                )
            )
        )
    }
}
