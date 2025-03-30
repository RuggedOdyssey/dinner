package data.dto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Ingredient(
    val name: String = "",
    val quantity: String = ""
)

@Serializable
data class Recipe(
    val description: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val title: String = ""
)

fun String.toRecipe() = Json { ignoreUnknownKeys = true }.decodeFromString<Recipe>(this)