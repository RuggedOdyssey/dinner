package domain

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
    ): Result<Recipe> {
        return try {
            // Parse ingredients from availableProducts
            val ingredients = availableProducts.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // Get recipe text using the existing getRecipe method
            val recipeText = getRecipe(photo, ingredients, recipeTitle, dietaryPreferences).first { 
                it != "Loading..." && !it.contains("Model not available") 
            }

            // Parse the recipe text into a Recipe object
            val recipe = parseRecipeText(recipeText, recipeTitle, ingredients)
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses the LLM output text into a structured Recipe object.
     * Also adds the input ingredients as part of the recipe ingredients.
     * Removes markdown formatting (bold and italic) from the text.
     */
    private fun parseRecipeText(response: String, recipeTitle: String?, ingredients: List<String>): Recipe {
        // Remove markdown bold and italic formatting
        val cleanedResponse = response
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Remove bold (**text**)
            .replace(Regex("\\*(.*?)\\*"), "$1")       // Remove italic (*text*)

        // Parse the LLM output into our data structure
        val lines = cleanedResponse.split("\n")

        // Extract title (use provided title or first line)
        val title = recipeTitle?.takeIf { it.isNotBlank() } 
            ?: lines.firstOrNull()?.trim() 
            ?: "Recipe"

        // Extract ingredients section using regex
        val ingredientsList = mutableListOf<Ingredient>()
        val ingredientSectionRegex = Regex("(?i)ingredients?:?\\s*\\n(.*?)(?=\\n\\s*(?:instructions?|directions?|steps?):?|$)", RegexOption.DOT_MATCHES_ALL)
        val ingredientMatch = ingredientSectionRegex.find(cleanedResponse)

        ingredientMatch?.groups?.get(1)?.value?.split("\n")?.forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotBlank()) {
                // Extract quantity and name using regex
                val ingredientRegex = Regex("^([\\d/.,\\s]+\\s*(?:g|kg|ml|l|cup|cups|tbsp|tsp|tablespoon|teaspoon|pinch|to taste|handful|piece|pieces)?)\\s*(.+)$")
                val match = ingredientRegex.find(trimmedLine)

                if (match != null) {
                    val quantity = match.groups[1]?.value?.trim() ?: ""
                    val name = match.groups[2]?.value?.trim() ?: trimmedLine
                    ingredientsList.add(Ingredient(name = name, quantity = quantity))
                } else {
                    // If regex doesn't match, fall back to simple space splitting
                    val parts = trimmedLine.split(" ", limit = 2)
                    if (parts.size > 1) {
                        ingredientsList.add(Ingredient(name = parts[1], quantity = parts[0]))
                    } else {
                        ingredientsList.add(Ingredient(name = trimmedLine, quantity = ""))
                    }
                }
            }
        }

        // Extract steps section using regex
        val steps = mutableListOf<String>()
        val stepsSectionRegex = Regex("(?i)(?:instructions?|directions?|steps?):?\\s*\\n(.*?)$", RegexOption.DOT_MATCHES_ALL)
        val stepsMatch = stepsSectionRegex.find(cleanedResponse)

        stepsMatch?.groups?.get(1)?.value?.split("\n")?.forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotBlank()) {
                // Remove step numbers if present
                val cleanedStep = trimmedLine.replace(Regex("^\\d+\\.?\\s*"), "")
                steps.add(cleanedStep)
            }
        }

        // Add the input ingredients to the recipe ingredients if they're not already included
        val inputIngredients = ingredients.map { inputIngredient ->
            // Check if this input ingredient is already in the parsed ingredients list
            val existingIngredient = ingredientsList.find { 
                it.name.contains(inputIngredient, ignoreCase = true) 
            }

            if (existingIngredient != null) {
                existingIngredient
            } else {
                // Add as a new ingredient with empty quantity
                Ingredient(name = inputIngredient, quantity = "")
            }
        }

        // Combine parsed ingredients with input ingredients, removing duplicates
        val allIngredients = (ingredientsList + inputIngredients).distinctBy { it.name.lowercase() }

        return Recipe(
            title = title,
            description = "Generated with on-device LLM",
            ingredients = allIngredients,
            steps = steps
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
