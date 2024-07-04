package data.dto
import kotlinx.serialization.Serializable

@Serializable
data class Output(
    val groceries: List<String> = emptyList(),
    val recipe: Recipe = Recipe()
)

@Serializable
data class Recipe(
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val title: String = ""
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