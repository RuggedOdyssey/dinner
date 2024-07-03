package data.dto
import kotlinx.serialization.Serializable

@Serializable
data class Output(
    val groceries: List<String>,
    val recipe: Recipe
)

@Serializable
data class Recipe(
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val title: String
)

const val outputFormat = """
output: {
    groceries: :string[],
    recipe {
        title: string,
        description: string,
        ingredients: string[],
        steps: string[]
    }
}
"""