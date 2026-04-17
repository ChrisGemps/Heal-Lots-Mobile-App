package com.heallots.mobile.features.auth.login

import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.LoginRequest
import com.heallots.mobile.models.LoginResponse
import com.heallots.mobile.storage.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun login(
        email: String,
        password: String,
        onSuccess: (LoginResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.login(LoginRequest(email, password)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    tokenManager.saveToken(body.token)
                    tokenManager.saveUser(body.user)
                    onSuccess(body)
                } else {
                    onError(
                        if (response.code() == 401) "Invalid email or password."
                        else "Login failed. Please try again."
                    )
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onError("Server error: ${t.message ?: "Unknown error"}")
            }
        })
    }
}
