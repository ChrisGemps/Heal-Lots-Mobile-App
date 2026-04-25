package com.heallots.mobile.utils

import android.content.SharedPreferences

/**
 * Get string value or null if not exists
 */
fun SharedPreferences.getStringOrNull(key: String): String? = getString(key, null)

/**
 * Get string value with fallback default
 */
fun SharedPreferences.getStringOrDefault(key: String, default: String): String =
    getString(key, default) ?: default

/**
 * Put string value (handles null by removing)
 */
fun SharedPreferences.putString(key: String, value: String?) {
    edit().apply {
        if (value == null) {
            remove(key)
        } else {
            putString(key, value)
        }
    }.apply()
}

/**
 * Remove key from SharedPreferences
 */
fun SharedPreferences.remove(key: String) {
    edit().remove(key).apply()
}

/**
 * Clear all data in SharedPreferences
 */
fun SharedPreferences.clearAll() {
    edit().clear().apply()
}

/**
 * Check if key exists
 */
fun SharedPreferences.contains(key: String): Boolean = contains(key)

/**
 * Get all keys
 */
fun SharedPreferences.getAllKeys(): Set<String> = all.keys

/**
 * Get all values as Map
 */
fun SharedPreferences.getAllValues(): Map<String, *> = all
