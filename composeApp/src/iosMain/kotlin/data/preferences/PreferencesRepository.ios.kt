package data.preferences

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of PreferencesRepository using NSUserDefaults.
 */
actual class PreferencesRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    /**
     * Save a boolean preference.
     */
    actual fun saveBoolean(key: String, value: Boolean) {
        userDefaults.setObject(value, key)
    }

    /**
     * Get a boolean preference, returning defaultValue if not found.
     */
    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return (userDefaults.objectForKey(key) as? Boolean) ?: defaultValue
    }
}
