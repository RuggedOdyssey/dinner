package domain

import data.preferences.PreferenceKeys
import data.preferences.PreferencesRepository

/** Data class representing dietary preferences. */
data class DietaryPreferences(
    val vegetarian: Boolean = false,
    val lactoseFree: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val noSeafood: Boolean = false,
    val noPeanuts: Boolean = false,
    val noPork: Boolean = false,
    val other: String = ""
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
        if (other.isNotEmpty()) preferences.add(other)

        return if (preferences.isEmpty()) {
            "no special dietary preferences"
        } else {
            "dietary preferences: ${preferences.joinToString(", ")}"
        }
    }

}

fun DietaryPreferences(preferencesRepository: PreferencesRepository): DietaryPreferences = DietaryPreferences(
    vegetarian = preferencesRepository.getBoolean(PreferenceKeys.VEGETARIAN, false),
    lactoseFree = preferencesRepository.getBoolean(PreferenceKeys.LACTOSE_FREE, false),
    vegan = preferencesRepository.getBoolean(PreferenceKeys.VEGAN, false),
    glutenFree = preferencesRepository.getBoolean(PreferenceKeys.GLUTEN_FREE, false),
    noSeafood = preferencesRepository.getBoolean(PreferenceKeys.NO_SEAFOOD, false),
    noPeanuts = preferencesRepository.getBoolean(PreferenceKeys.NO_PEANUTS, false),
    noPork = preferencesRepository.getBoolean(PreferenceKeys.NO_PORK, false),
    other = preferencesRepository.getString(PreferenceKeys.OTHER, "")
)
