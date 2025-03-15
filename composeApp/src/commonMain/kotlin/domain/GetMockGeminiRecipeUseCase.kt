package domain

import data.dto.Output
import data.dto.Recipe

class GetMockGeminiRecipeUseCase : RecipeUseCase {

    override suspend operator fun invoke(photo: ByteArray, availableProducts: String): Result<Output> {
        // Return a mock response
        return Result.success(createMockOutput(availableProducts))
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
}
