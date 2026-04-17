package com.heallots.mobile.features.auth.login

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
import com.heallots.mobile.features.admin.dashboard.AdminDashboardActivity
import com.heallots.mobile.features.appointments.dashboard.DashboardActivity
import com.heallots.mobile.features.auth.register.RegisterActivity
import com.heallots.mobile.storage.TokenManager

class LoginActivity : AppCompatActivity(), LoginContract.View {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordToggleButton: ImageButton
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var errorBanner: FrameLayout
    private lateinit var errorMessage: TextView

    private lateinit var presenter: LoginContract.Presenter
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_login)

            presenter = LoginPresenter(
                view = this,
                repository = LoginRepository(
                    apiService = ApiClient.getApiService(),
                    tokenManager = TokenManager(this)
                )
            )

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
            registerLink.setOnClickListener { presenter.onRegisterClicked() }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            finish()
        }
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
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

    override fun showError(message: String) {
        errorMessage.text = message
        errorBanner.visibility = View.VISIBLE
    }

    override fun hideError() {
        errorBanner.visibility = View.GONE
    }

    private fun handleLogin() {
        hideKeyboard()
        presenter.onLoginClicked(
            email = emailEditText.text.toString(),
            password = passwordEditText.text.toString()
        )
    }

    private fun hideKeyboard() {
        val imm = getSystemService(InputMethodManager::class.java)
        val current = currentFocus
        if (imm != null && current != null) {
            imm.hideSoftInputFromWindow(current.windowToken, 0)
        }
    }

    override fun showLoading() {
        loginButton.isEnabled = false
        loginButton.text = "Signing in..."
    }

    override fun hideLoading() {
        loginButton.isEnabled = true
        loginButton.text = "Sign In"
    }

    override fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    override fun navigateToDashboard(isAdmin: Boolean) {
        val intent = if (isAdmin) {
            Intent(this, AdminDashboardActivity::class.java)
        } else {
            Intent(this, DashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
