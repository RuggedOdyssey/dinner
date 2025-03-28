package data.preferences

/**
 * Repository for storing and retrieving user preferences across platforms.
 */
expect class PreferencesRepository() {
    /**
     * Save a boolean preference.
     */
    fun saveBoolean(key: String, value: Boolean)

    /**
     * Get a boolean preference, returning defaultValue if not found.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
}

/**
 * Keys for dietary preferences.
 */
object PreferenceKeys {
    const val VEGETARIAN = "pref_vegetarian"
    const val LACTOSE_FREE = "pref_lactose_free"
    const val VEGAN = "pref_vegan"
    const val GLUTEN_FREE = "pref_gluten_free"
    const val NO_SEAFOOD = "pref_no_seafood"
    const val NO_PEANUTS = "pref_no_peanuts"
    const val NO_PORK = "pref_no_pork"
}