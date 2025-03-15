package domain

import data.dto.Output

/**
 * Interface for recipe use cases that process input and return recipe output.
 */
interface RecipeUseCase {
    /**
     * Process the input and return a recipe output.
     *
     * @param photo The photo containing ingredients or food items
     * @param availableProducts Additional text input about available products
     * @return Result containing the Output with recipe and groceries information
     */
    suspend operator fun invoke(photo: ByteArray, availableProducts: String): Result<Output>
}