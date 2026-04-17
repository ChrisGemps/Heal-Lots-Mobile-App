package com.heallots.mobile.features.auth.register

import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.LoginResponse
import com.heallots.mobile.models.RegisterRequest
import com.heallots.mobile.storage.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun register(
        request: RegisterRequest,
        onSuccess: (LoginResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.register(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    tokenManager.saveToken(body.token)
                    tokenManager.saveUser(body.user)
                    onSuccess(body)
                } else {
                    onError(if (response.code() == 409) "Email already registered" else "Registration failed")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onError("Error: ${t.message ?: "Unknown error"}")
            }
        })
    }
}
