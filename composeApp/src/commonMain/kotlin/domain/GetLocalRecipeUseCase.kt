package domain

import data.dto.Output
import data.dto.Recipe
import data.dto.Ingredient
import data.llminference.LLMFactory
import data.llminference.LLMProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import domain.RecipePrompt

class GetLocalRecipeUseCase(private val llmFactory: LLMFactory) : RecipeUseCase {
    private val llmProcessor: LLMProcessor by lazy { llmFactory.createLLMProcessor() }

    /**
     * Implementation of the RecipeUseCase interface.
     * Processes the photo and available products to generate a recipe.
     */
    override suspend operator fun invoke(
        photo: ByteArray, 
        availableProducts: String, 
        recipeTitle: String?,
        dietaryPreferences: DietaryPreferences
    ): Result<Output> {
        return try {
            // Parse ingredients from availableProducts
            val ingredients = availableProducts.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // Get recipe text using the existing getRecipe method
            val recipeText = getRecipe(photo, ingredients, recipeTitle, dietaryPreferences).first { 
                it != "Loading..." && !it.contains("Model not available") 
            }

            // Parse the recipe text into an Output object
            val output = parseRecipeText(recipeText, recipeTitle, ingredients)
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses the LLM output text into a structured Output object.
     */
    private fun parseRecipeText(response: String, recipeTitle: String?, ingredients: List<String>): Output {
        // Parse the LLM output into our data structure
        val lines = response.split("\n")
        val title = recipeTitle?.takeIf { it.isNotBlank() } ?: lines.firstOrNull() ?: "Recipe"

        val ingredientsList = mutableListOf<Ingredient>()
        val steps = mutableListOf<String>()

        var inIngredients = false
        var inSteps = false

        lines.forEach { line ->
            when {
                line.contains("Ingredients", ignoreCase = true) && !inIngredients && !inSteps -> {
                    inIngredients = true
                }
                line.contains("Instructions", ignoreCase = true) || 
                    line.contains("Directions", ignoreCase = true) || 
                    line.contains("Steps", ignoreCase = true) -> {
                    inIngredients = false
                    inSteps = true
                }
                inIngredients && line.isNotBlank() && !line.contains("Ingredients", ignoreCase = true) -> {
                    val trimmedLine = line.trim()
                    // Try to split the ingredient line into quantity and name
                    val parts = trimmedLine.split(" ", limit = 2)
                    if (parts.size > 1) {
                        ingredientsList.add(Ingredient(name = parts[1], quantity = parts[0]))
                    } else {
                        ingredientsList.add(Ingredient(name = trimmedLine, quantity = ""))
                    }
                }
                inSteps && line.isNotBlank() && 
                    !line.contains("Instructions", ignoreCase = true) && 
                    !line.contains("Directions", ignoreCase = true) && 
                    !line.contains("Steps", ignoreCase = true) -> {
                    steps.add(line.trim())
                }
            }
        }

        val recipe = Recipe(
            title = title,
            description = "Generated with on-device LLM",
            ingredients = ingredientsList,
            steps = steps
        )

        return Output(
            groceries = ingredients.map { Ingredient(name = it, quantity = "") },
            recipe = recipe
        )
    }

    /**
     * Original method for getting a recipe based on ingredients.
     * Returns a flow of strings representing the recipe generation progress and result.
     */
    private fun getRecipe(
        photo: ByteArray, 
        ingredients: List<String>, 
        recipeTitle: String? = null,
        dietaryPreferences: DietaryPreferences = DietaryPreferences()
    ): Flow<String> = flow {
        emit("Loading...")

        // Check if model is available
        if (!llmProcessor.isModelAvailable()) {
            emit("Model not available. Please ensure the model is installed.")
            return@flow
        }

        val ingredientsText = ingredients.joinToString(", ")
        val prompt = RecipePrompt.makePrompt(ingredientsText, recipeTitle, dietaryPreferences)

        val response = llmProcessor.generateText(photo, prompt)
        emit(response)
    }
}
