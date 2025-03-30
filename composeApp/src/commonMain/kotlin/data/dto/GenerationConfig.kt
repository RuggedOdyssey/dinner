package data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class GenerationConfig(
    val maxOutputTokens: Int = 8192, 
    val temperature: Double = 1.0, 
    val topP: Double = 0.95,
    val responseMimeType: String = "application/json",
    val responseSchema: JsonObject = buildRecipeSchema()
)

private fun buildRecipeSchema(): JsonObject = buildJsonObject {
    put("type", "object")
    putJsonObject("properties") {
        putJsonObject("title") {
            put("type", "string")
        }
        putJsonObject("description") {
            put("type", "string")
        }
        putJsonObject("ingredients") {
            put("type", "array")
            putJsonObject("items") {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("name") {
                        put("type", "string")
                    }
                    putJsonObject("quantity") {
                        put("type", "string")
                    }
                }
                putJsonArray("required") {
                    add(JsonPrimitive("name"))
                    add(JsonPrimitive("quantity"))
                }
            }
        }
        putJsonObject("steps") {
            put("type", "array")
            putJsonObject("items") {
                put("type", "string")
            }
        }
    }
    putJsonArray("required") {
        add(JsonPrimitive("title"))
        add(JsonPrimitive("description"))
        add(JsonPrimitive("ingredients"))
        add(JsonPrimitive("steps"))
    }
}
