package domain

import data.VertexService
import data.dto.Recipe
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import domain.RecipePrompt

/**
 * Implementation of [RecipeUseCase] that uses Google's Gemini API to generate recipes.
 * 
 * This class processes photos of ingredients and user input to generate recipes using
 * the Gemini multimodal Large Language Model through the Vertex AI service.
 * 
 * @property service The [VertexService] used to communicate with the Gemini API
 */
@OptIn(ExperimentalEncodingApi::class)
class GetGeminiRecipeUseCase(private val service: VertexService = VertexService()) : RecipeUseCase {

    /**
     * Implementation of the RecipeUseCase interface that uses the Gemini API.
     * Encodes the photo as Base64, creates a prompt using the available products,
     * recipe title, and dietary preferences, and sends it to the Gemini API to
     * generate a recipe.
     *
     * @param photo The photo containing ingredients or food items, sent to Gemini for analysis
     * @param availableProducts Additional text input about available products
     * @param recipeTitle Optional title for the recipe
     * @param dietaryPreferences Dietary preferences to consider for the recipe
     * @return Result containing the Recipe information from Gemini API or an error
     */
    override suspend operator fun invoke(
        photo: ByteArray, 
        availableProducts: String, 
        recipeTitle: String?,
        dietaryPreferences: DietaryPreferences
    ): Result<Recipe> {
        // Use the online service
        val encodedBitmap = Base64.encode(photo)
        val prompt = RecipePrompt.makePrompt(availableProducts, recipeTitle, dietaryPreferences)
        return service.getGeminiResponse(encodedBitmap, prompt)
    }
}
