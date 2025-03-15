package domain

import data.dto.Output
import data.dto.Recipe
import data.dto.toOutput
import data.mediapipe.LLMFactory
import data.mediapipe.LLMProcessor
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
    override suspend operator fun invoke(photo: ByteArray, availableProducts: String): Result<Output> {
        return try {
            // Parse ingredients from availableProducts
            val ingredients = availableProducts.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // Get recipe text using the existing getRecipe method
            val recipeText = getRecipe(ingredients).first { it != "Loading..." && !it.contains("Downloading model") }

            // Parse the recipe text into an Output object
//            val output = parseRecipeText(recipeText, ingredients)
            val output = recipeText.toOutput()
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Original method for getting a recipe based on ingredients.
     * Returns a flow of strings representing the recipe generation progress and result.
     */
    fun getRecipe(ingredients: List<String>): Flow<String> = flow {
        emit("Loading...")

        // Check if model is available, download if needed
        if (!llmProcessor.isModelAvailable()) {
            emit("Downloading model...")
            llmProcessor.downloadModelIfNeeded()
        }

        val ingredientsText = ingredients.joinToString(", ")
        val prompt = RecipePrompt.makePrompt(ingredientsText)

        val response = llmProcessor.generateText(prompt)
        emit(response)
    }

    /**
     * Parse the recipe text into an Output object.
     */
    private fun parseRecipeText(recipeText: String, originalIngredients: List<String>): Output {
        // TODO this is doing weird things, debug needed
        // Simple parsing logic - in a real app, you'd want more robust parsing
        val lines = recipeText.split("\n")
        val title = lines.firstOrNull() ?: "Recipe"

        val ingredientsList = mutableListOf<String>()
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
                    ingredientsList.add(line.trim())
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
            groceries = originalIngredients,
            recipe = recipe
        )
    }
}
