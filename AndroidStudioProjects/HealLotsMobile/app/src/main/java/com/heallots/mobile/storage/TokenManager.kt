package com.heallots.mobile.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.heallots.mobile.models.User

class TokenManager(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val gson = Gson()

    init {
        sharedPreferences = try {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveToken(token: String?) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? = sharedPreferences.getString(TOKEN_KEY, null)

    fun saveUser(user: User?) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(USER_KEY, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(USER_KEY, null) ?: return null
        return gson.fromJson(userJson, User::class.java)
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }

    fun getAuthorizationHeader(): String? = getToken()?.let { "Bearer $it" }

    companion object {
        private const val PREFS_NAME = "HealLotsPrefs"
        private const val TOKEN_KEY = "token"
        private const val USER_KEY = "user"
    }
}
