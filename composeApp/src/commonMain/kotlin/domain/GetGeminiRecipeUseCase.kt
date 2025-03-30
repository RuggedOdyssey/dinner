package domain

import data.VertexService
import data.dto.Recipe
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import domain.RecipePrompt

@OptIn(ExperimentalEncodingApi::class)
class GetGeminiRecipeUseCase(private val service: VertexService = VertexService()) : RecipeUseCase {

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
