package com.heallots.mobile.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.heallots.mobile.R
import com.heallots.mobile.api.ApiClient
import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.LoginRequest
import com.heallots.mobile.models.LoginResponse
import com.heallots.mobile.storage.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordToggleButton: ImageButton
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var errorBanner: FrameLayout
    private lateinit var errorMessage: TextView

    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_login)

            apiService = ApiClient.getApiService()
            tokenManager = TokenManager(this)

            emailEditText = findViewById(R.id.emailEditText)
            passwordEditText = findViewById(R.id.passwordEditText)
            passwordToggleButton = findViewById(R.id.passwordToggleButton)
            loginButton = findViewById(R.id.loginButton)
            registerLink = findViewById(R.id.registerLink)
            errorBanner = findViewById(R.id.errorBanner)
            errorMessage = findViewById(R.id.errorMessage)

            passwordToggleButton.setOnClickListener { togglePasswordVisibility() }

            emailEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) hideError()
            }
            passwordEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) hideError()
            }

            loginButton.setOnClickListener { handleLogin() }
            registerLink.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            finish()
        }
    }

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        passwordEditText.inputType = if (passwordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        passwordToggleButton.setImageResource(android.R.drawable.ic_menu_view)
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun showError(message: String) {
        errorMessage.text = message
        errorBanner.visibility = View.VISIBLE
    }

    private fun hideError() {
        errorBanner.visibility = View.GONE
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (!validateInputs(email, password)) {
            return
        }

        hideKeyboard()
        loginButton.isEnabled = false
        loginButton.text = "Signing in..."
        hideError()

        val request = LoginRequest(email, password)
        apiService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val loginResponse = response.body()
                if (response.isSuccessful && loginResponse != null) {
                    tokenManager.saveToken(loginResponse.token)
                    tokenManager.saveUser(loginResponse.user)

                    val role = loginResponse.user?.role
                    val intent = if (role == "ADMIN") {
                        Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                    } else {
                        Intent(this@LoginActivity, DashboardActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    loginButton.isEnabled = true
                    loginButton.text = "Sign In"
                    showError(
                        if (response.code() == 401) "Invalid email or password."
                        else "Login failed. Please try again."
                    )
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "Login error", t)
                loginButton.isEnabled = true
                loginButton.text = "Sign In"
                showError("Server error: ${t.message ?: "Unknown error"}")
            }
        })
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showError("Please enter your email address.")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Please enter a valid email address.")
                false
            }
            password.isEmpty() -> {
                showError("Please enter your password.")
                false
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters.")
                false
            }
            else -> true
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(InputMethodManager::class.java)
        val current = currentFocus
        if (imm != null && current != null) {
            imm.hideSoftInputFromWindow(current.windowToken, 0)
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
