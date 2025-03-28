package domain

/**
 * Data class representing dietary preferences.
 */
data class DietaryPreferences(
    val vegetarian: Boolean = false,
    val lactoseFree: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val noSeafood: Boolean = false,
    val noPeanuts: Boolean = false,
    val noPork: Boolean = false
) {
    override fun toString(): String {
        val preferences = mutableListOf<String>()
        
        if (vegetarian) preferences.add("vegetarian")
        if (lactoseFree) preferences.add("lactose-free")
        if (vegan) preferences.add("vegan")
        if (glutenFree) preferences.add("gluten-free")
        if (noSeafood) preferences.add("no seafood")
        if (noPeanuts) preferences.add("no peanuts")
        if (noPork) preferences.add("no pork")
        
        return if (preferences.isEmpty()) {
            "no special dietary preferences"
        } else {
            "dietary preferences: ${preferences.joinToString(", ")}"
        }
    }
}