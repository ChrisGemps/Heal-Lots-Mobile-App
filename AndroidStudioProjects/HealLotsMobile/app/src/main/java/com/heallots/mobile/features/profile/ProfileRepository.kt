package com.heallots.mobile.features.profile

import android.content.Context
import android.net.Uri
import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.User
import com.heallots.mobile.storage.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun getCurrentUser(): User? = tokenManager.getUser()

    fun updateProfile(user: User, onSuccess: (User) -> Unit, onError: (String) -> Unit) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onError("Please sign in again to update your profile")
            return
        }
        apiService.updateProfile(authHeader, user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    persistUser(body)
                    onSuccess(body)
                } else {
                    onError("Failed to save profile changes")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                onError("Failed to save profile changes")
            }
        })
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onError("Please sign in again to update your password")
            return
        }
        apiService.changePassword(
            authHeader,
            hashMapOf("currentPassword" to currentPassword, "newPassword" to newPassword)
        ).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) onSuccess() else onError("Failed to update password")
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                onError("Failed to update password")
            }
        })
    }

    fun uploadProfilePicture(uri: Uri, onSuccess: (User) -> Unit, onError: (String) -> Unit) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onError("Please sign in again to upload a photo")
            return
        }
        try {
            val bytes = readBytes(uri)
            if (bytes == null || bytes.isEmpty()) {
                onError("Unable to read the selected image")
                return
            }
            val mimeType = context.contentResolver.getType(uri)
            val requestBody = bytes.toRequestBody((mimeType ?: "image/*").toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", "profile-image.jpg", requestBody)
            apiService.uploadProfilePicture(authHeader, part).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        persistUser(body)
                        onSuccess(body)
                    } else {
                        onError("Failed to upload profile picture")
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    onError("Failed to upload profile picture")
                }
            })
        } catch (_: Exception) {
            onError("Failed to open selected image")
        }
    }

    fun logout() {
        tokenManager.logout()
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun persistUser(user: User) {
        tokenManager.saveUser(user)
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit()
            .putString("fullName", safeText(user.fullName, "User"))
            .putString("email", safeText(user.email, ""))
            .putString("phone", safeText(user.getPhone(), ""))
            .putString("birthday", safeText(user.birthday, ""))
            .putString("gender", safeText(user.gender, ""))
            .putString("address", safeText(user.address, ""))
            .putString("profilePictureUrl", safeText(user.profilePictureUrl, ""))
            .putString("role", safeText(user.role, "USER"))
            .apply()
    }

    private fun readBytes(uri: Uri): ByteArray? {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        return outputStream.toByteArray()
    }

    private fun safeText(value: String?, fallback: String): String = if (value.isNullOrBlank()) fallback else value
}
