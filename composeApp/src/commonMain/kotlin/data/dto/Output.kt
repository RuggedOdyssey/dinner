package data.dto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Output(
    val groceries: List<String> = emptyList(),
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

const val outputFormat = """
output: {
    groceries: :string[],
    recipe {
        title: string,
        description: string,
        ingredients: {
            name: string,
            quantity: string
        }[],
        steps: string[]
    }
}
"""

fun String.toOutput() = Json { ignoreUnknownKeys = true }.decodeFromString<Output>(this)
