package domain

import data.dto.Recipe

/**
 * Interface for recipe use cases that process input and return recipe output.
 */
interface RecipeUseCase {
    /**
     * Process the input and return a recipe.
     *
     * @param photo The photo containing ingredients or food items
     * @param availableProducts Additional text input about available products
     * @param recipeTitle Optional title for the recipe
     * @param dietaryPreferences Dietary preferences to consider for the recipe
     * @return Result containing the Recipe information
     */
    suspend operator fun invoke(
        photo: ByteArray, 
        availableProducts: String, 
        recipeTitle: String? = null,
        dietaryPreferences: DietaryPreferences = DietaryPreferences()
    ): Result<Recipe>
}
