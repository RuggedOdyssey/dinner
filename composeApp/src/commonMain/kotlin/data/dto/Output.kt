package data.dto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Output(
    val groceries: List<Ingredient> = emptyList(),
    val recipe: Recipe = Recipe()
)

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

fun String.toOutput() = Json { ignoreUnknownKeys = true }.decodeFromString<Output>(this)
