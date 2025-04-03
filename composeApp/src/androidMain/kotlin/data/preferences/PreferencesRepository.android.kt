package data.preferences

import android.content.Context
import android.content.SharedPreferences
import net.ruggedodyssey.whatsfordinner.MainActivity

/**
 * Android implementation of PreferencesRepository using SharedPreferences.
 */
actual class PreferencesRepository {
    private val sharedPreferences: SharedPreferences = MainActivity.getInstance().getSharedPreferences(
        "dietary_preferences",
        Context.MODE_PRIVATE
    )

    /**
     * Save a boolean preference.
     */
    actual fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Get a boolean preference, returning defaultValue if not found.
     */
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    /**
     * Save a string preference.
     */
    actual fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    /**
     * Get a string preference, returning defaultValue if not found.
     */
    actual fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
}
