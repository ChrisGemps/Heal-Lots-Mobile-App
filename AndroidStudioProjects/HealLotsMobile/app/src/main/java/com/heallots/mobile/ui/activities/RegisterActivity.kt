package com.heallots.mobile.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.heallots.mobile.R
import com.heallots.mobile.api.ApiClient
import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.LoginResponse
import com.heallots.mobile.models.RegisterRequest
import com.heallots.mobile.storage.TokenManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var birthdayEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var errorBanner: FrameLayout
    private lateinit var errorMessage: TextView
    private lateinit var passwordToggleButton: ImageButton
    private lateinit var confirmPasswordToggleButton: ImageButton
    private lateinit var passwordStrengthBar: View
    private lateinit var passwordStrengthText: TextView

    private var passwordVisible = false
    private var confirmPasswordVisible = false
    private var isFormatting = false
    private var selectedBirthday = ""

    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_register)

            apiService = ApiClient.getApiService()
            tokenManager = TokenManager(this)

            initializeViews()
            setupListeners()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            finish()
        }
    }

    private fun initializeViews() {
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        birthdayEditText = findViewById(R.id.birthdayEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        addressEditText = findViewById(R.id.addressEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)
        errorBanner = findViewById(R.id.errorBanner)
        errorMessage = findViewById(R.id.errorMessage)
        passwordToggleButton = findViewById(R.id.passwordToggleButton)
        confirmPasswordToggleButton = findViewById(R.id.confirmPasswordToggleButton)
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar)
        passwordStrengthText = findViewById(R.id.passwordStrengthText)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener { handleRegister() }

        loginLink.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }

        passwordToggleButton.setOnClickListener { togglePasswordVisibility() }
        confirmPasswordToggleButton.setOnClickListener { toggleConfirmPasswordVisibility() }

        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable) {
                formatPhoneNumber(s)
            }
        })

        birthdayEditText.inputType = InputType.TYPE_NULL
        birthdayEditText.setOnClickListener {
            hideError()
            showDatePicker()
        }
        birthdayEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideError()
                showDatePicker()
            }
        }

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable) {
                updatePasswordStrength()
            }
        })

        nameEditText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) hideError() }
        emailEditText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) hideError() }
        phoneEditText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) hideError() }
        passwordEditText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) hideError() }
        confirmPasswordEditText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) hideError() }

        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter
    }

    private fun formatPhoneNumber(editable: Editable) {
        if (isFormatting) return
        isFormatting = true

        var text = editable.toString().replace(Regex("[^0-9]"), "")
        if (text.length > 11) {
            text = text.substring(0, 11)
        }

        val formatted = when {
            text.isEmpty() -> ""
            text.length <= 4 -> text
            text.length <= 7 -> "${text.substring(0, 4)} ${text.substring(4)}"
            else -> "${text.substring(0, 4)} ${text.substring(4, 7)} ${text.substring(7)}"
        }

        if (editable.toString() != formatted) {
            editable.clear()
            editable.append(formatted)
        }

        isFormatting = false
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val monthStr = String.format(Locale.US, "%02d", month + 1)
                val dayStr = String.format(Locale.US, "%02d", dayOfMonth)
                val yearStr = String.format(Locale.US, "%04d", year)
                selectedBirthday = monthStr + dayStr + yearStr

                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val displayFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                birthdayEditText.setText(displayFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun updatePasswordStrength() {
        val password = passwordEditText.text.toString().trim()
        val length = password.length
        val hasUppercase = password.matches(Regex(".*[A-Z].*"))
        val hasNumber = password.matches(Regex(".*\\d.*"))

        val strengthColor: Int
        val strengthText: String
        val fillPercent: Float

        when {
            length < 6 -> {
                strengthColor = resources.getColor(R.color.strength_too_short)
                strengthText = "Too short"
                fillPercent = 0.25f
            }
            length in 6..8 && (!hasUppercase || !hasNumber) -> {
                strengthColor = resources.getColor(R.color.strength_weak)
                strengthText = "Weak"
                fillPercent = 0.50f
            }
            length > 8 && (!hasUppercase || !hasNumber) -> {
                strengthColor = resources.getColor(R.color.strength_fair)
                strengthText = "Fair"
                fillPercent = 0.70f
            }
            else -> {
                strengthColor = resources.getColor(R.color.strength_strong)
                strengthText = "Strong"
                fillPercent = 1.0f
            }
        }

        try {
            val params = passwordStrengthBar.layoutParams as? LinearLayout.LayoutParams
            if (params != null) {
                val parent = passwordStrengthBar.parent as? ViewGroup
                var parentWidth = parent?.width ?: 0

                if (parentWidth <= 0) {
                    val screenWidth = resources.displayMetrics.widthPixels
                    parentWidth = (screenWidth * 0.8f).toInt()
                }

                params.width = (parentWidth * fillPercent).toInt()
                passwordStrengthBar.layoutParams = params
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password strength bar: ${e.message}")
        }

        passwordStrengthBar.setBackgroundColor(strengthColor)
        passwordStrengthText.text = strengthText
        passwordStrengthText.setTextColor(strengthColor)
    }

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        passwordEditText.inputType = if (passwordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible
        confirmPasswordEditText.inputType = if (confirmPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
    }

    private fun showError(message: String) {
        errorMessage.text = message
        errorBanner.visibility = View.VISIBLE
    }

    private fun hideError() {
        errorBanner.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = currentFocus
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun handleRegister() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim().replace(" ", "")
        val birthday = selectedBirthday
        val gender = genderSpinner.selectedItem.toString()
        val address = addressEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (!validateInputs(name, email, phone, birthday, gender, address, password, confirmPassword)) {
            return
        }

        hideKeyboard()
        registerButton.isEnabled = false
        registerButton.text = "Creating Account..."

        val request = RegisterRequest(name, email, phone, birthday, gender, address, password)

        apiService.register(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val registerResponse = response.body()
                if (response.isSuccessful && registerResponse != null) {
                    tokenManager.saveToken(registerResponse.token)
                    tokenManager.saveUser(registerResponse.user)

                    Log.d(TAG, "Registration successful")
                    startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    val errorMsg = if (response.code() == 409) "Email already registered" else "Registration failed"
                    showError(errorMsg)
                    Log.e(TAG, "Registration error: ${response.code()}")
                    registerButton.isEnabled = true
                    registerButton.text = "Create Account"
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showError("Error: ${t.message ?: "Unknown error"}")
                Log.e(TAG, "Registration failed", t)
                registerButton.isEnabled = true
                registerButton.text = "Create Account"
            }
        })
    }

    private fun validateInputs(
        name: String,
        email: String,
        phone: String,
        birthday: String,
        gender: String,
        address: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                showError("Full name is required")
                false
            }
            name.length < 2 -> {
                showError("Name must be at least 2 characters")
                false
            }
            email.isEmpty() -> {
                showError("Email address is required")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Invalid email format (e.g., maria@example.com)")
                false
            }
            phone.isEmpty() || phone.length < 10 -> {
                showError("Phone number must be at least 10 digits")
                false
            }
            birthday.isEmpty() || birthday.length != 8 -> {
                showError("Birthday must be complete (MM/DD/YYYY)")
                false
            }
            gender == "Select Gender" -> {
                showError("Please select a gender")
                false
            }
            address.isEmpty() -> {
                showError("Address is required")
                false
            }
            address.length < 5 -> {
                showError("Address must be at least 5 characters")
                false
            }
            password.isEmpty() -> {
                showError("Password is required")
                false
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters")
                false
            }
            confirmPassword.isEmpty() -> {
                showError("Please confirm your password")
                false
            }
            password != confirmPassword -> {
                showError("Passwords do not match")
                false
            }
            else -> true
        }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}
