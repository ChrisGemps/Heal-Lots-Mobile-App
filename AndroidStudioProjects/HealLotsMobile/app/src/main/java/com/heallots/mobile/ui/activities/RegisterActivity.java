package com.heallots.mobile.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.heallots.mobile.R;
import com.heallots.mobile.api.ApiClient;
import com.heallots.mobile.api.ApiService;
import com.heallots.mobile.models.LoginResponse;
import com.heallots.mobile.models.RegisterRequest;
import com.heallots.mobile.storage.TokenManager;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // Form fields
    private EditText nameEditText, emailEditText, phoneEditText, passwordEditText, confirmPasswordEditText;
    private EditText birthdayEditText, addressEditText;
    private Spinner genderSpinner;
    private Button registerButton;
    private TextView loginLink;
    private FrameLayout errorBanner;
    private TextView errorMessage;
    private ImageButton passwordToggleButton, confirmPasswordToggleButton;
    private View passwordStrengthBar;
    private TextView passwordStrengthText;

    // State
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;
    private boolean isFormatting = false;
    private String selectedBirthday = ""; // Stores birthday as MMDDYYYY for API

    // API & Storage
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_register);

            apiService = ApiClient.getApiService();
            tokenManager = new TokenManager(this);

            // Initialize views
            initializeViews();

            if (!viewsInitialized()) {
                Log.e(TAG, "One or more views are null!");
                finish();
                return;
            }

            setupListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        genderSpinner = findViewById(R.id.genderSpinner);
        addressEditText = findViewById(R.id.addressEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        errorBanner = findViewById(R.id.errorBanner);
        errorMessage = findViewById(R.id.errorMessage);
        passwordToggleButton = findViewById(R.id.passwordToggleButton);
        confirmPasswordToggleButton = findViewById(R.id.confirmPasswordToggleButton);
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        passwordStrengthText = findViewById(R.id.passwordStrengthText);
    }

    private boolean viewsInitialized() {
        return nameEditText != null && emailEditText != null && phoneEditText != null &&
                birthdayEditText != null && genderSpinner != null && addressEditText != null &&
                passwordEditText != null && confirmPasswordEditText != null &&
                registerButton != null && loginLink != null && errorBanner != null &&
                errorMessage != null && passwordToggleButton != null && confirmPasswordToggleButton != null &&
                passwordStrengthBar != null && passwordStrengthText != null;
    }

    private void setupListeners() {
        // Register button
        registerButton.setOnClickListener(v -> handleRegister());

        // Login link
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Password toggle buttons
        passwordToggleButton.setOnClickListener(v -> togglePasswordVisibility());
        confirmPasswordToggleButton.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        // Phone number auto-formatting
        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                formatPhoneNumber(s);
            }
        });

        // Birthday field: open date picker on click
        birthdayEditText.setInputType(InputType.TYPE_NULL); // Disable keyboard input
        birthdayEditText.setOnClickListener(v -> {
            hideError();
            showDatePicker();
        });
        birthdayEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                hideError();
                showDatePicker();
            }
        });

        // Password strength indicator
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordStrength();
            }
        });

        // Clear error on field focus
        nameEditText.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) hideError(); });
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) hideError(); });
        phoneEditText.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) hideError(); });
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) hideError(); });
        confirmPasswordEditText.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) hideError(); });

        // Setup Gender Spinner
        String[] genderOptions = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
    }

    private void formatPhoneNumber(Editable editable) {
        if (isFormatting) return;
        isFormatting = true;

        String text = editable.toString().replaceAll("[^0-9]", "");

        if (text.length() > 11) {
            text = text.substring(0, 11);
        }

        String formatted = "";
        if (text.length() > 0) {
            if (text.length() <= 4) {
                formatted = text;
            } else if (text.length() <= 7) {
                formatted = text.substring(0, 4) + " " + text.substring(4);
            } else {
                formatted = text.substring(0, 4) + " " + text.substring(4, 7) + " " + text.substring(7);
            }
        }

        if (!editable.toString().equals(formatted)) {
            editable.clear();
            editable.append(formatted);
        }

        isFormatting = false;
    }

    /**
     * Show DatePickerDialog for birthday selection
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                // Store in MMDDYYYY format for API
                String monthStr = String.format(Locale.US, "%02d", month + 1); // month is 0-indexed
                String dayStr = String.format(Locale.US, "%02d", dayOfMonth);
                String yearStr = String.format(Locale.US, "%04d", year);
                selectedBirthday = monthStr + dayStr + yearStr;
                
                // Display in "Month DD, YYYY" format
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
                birthdayEditText.setText(displayFormat.format(selectedDate.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void updatePasswordStrength() {
        String password = passwordEditText.getText().toString().trim();
        int length = password.length();
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");

        int strengthColor;
        String strengthText;
        float fillPercent;

        if (length < 6) {
            strengthColor = getResources().getColor(R.color.strength_too_short);
            strengthText = "Too short";
            fillPercent = 0.25f;
        } else if (length >= 6 && length <= 8 && (!hasUppercase || !hasNumber)) {
            strengthColor = getResources().getColor(R.color.strength_weak);
            strengthText = "Weak";
            fillPercent = 0.50f;
        } else if (length > 8 && (!hasUppercase || !hasNumber)) {
            strengthColor = getResources().getColor(R.color.strength_fair);
            strengthText = "Fair";
            fillPercent = 0.70f;
        } else {
            strengthColor = getResources().getColor(R.color.strength_strong);
            strengthText = "Strong";
            fillPercent = 1.0f;
        }

        // Update strength bar width using LinearLayout.LayoutParams
        try {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) passwordStrengthBar.getLayoutParams();
            if (params != null) {
                android.view.ViewGroup parent = (android.view.ViewGroup) passwordStrengthBar.getParent();
                int parentWidth = parent != null ? parent.getWidth() : 0;

                if (parentWidth <= 0) {
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    parentWidth = (int) (screenWidth * 0.8);
                }

                int barWidth = (int) (parentWidth * fillPercent);
                params.width = barWidth;
                passwordStrengthBar.setLayoutParams(params);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating password strength bar: " + e.getMessage());
        }

        passwordStrengthBar.setBackgroundColor(strengthColor);
        passwordStrengthText.setText(strengthText);
        passwordStrengthText.setTextColor(strengthColor);
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;
        if (confirmPasswordVisible) {
            confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorBanner.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorBanner.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void handleRegister() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim().replaceAll(" ", "");
        String birthday = selectedBirthday; // Use the stored MMDDYYYY format
        String gender = genderSpinner.getSelectedItem().toString();
        String address = addressEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validateInputs(name, email, phone, birthday, gender, address, password, confirmPassword)) {
            return;
        }

        hideKeyboard();
        registerButton.setEnabled(false);
        registerButton.setText("Creating Account...");

        RegisterRequest request = new RegisterRequest(name, email, phone, birthday, gender, address, password);

        apiService.register(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse registerResponse = response.body();
                    tokenManager.saveToken(registerResponse.getToken());
                    tokenManager.saveUser(registerResponse.getUser());

                    Log.d(TAG, "Registration successful");
                    startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    String errorMsg = "Registration failed";
                    if (response.code() == 409) {
                        errorMsg = "Email already registered";
                    }
                    showError(errorMsg);
                    Log.e(TAG, "Registration error: " + response.code());
                    registerButton.setEnabled(true);
                    registerButton.setText("Create Account");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showError("Error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
                Log.e(TAG, "Registration failed", t);
                registerButton.setEnabled(true);
                registerButton.setText("Create Account");
            }
        });
    }

    private boolean validateInputs(String name, String email, String phone, String birthday,
                                   String gender, String address, String password, String confirmPassword) {
        if (name.isEmpty()) {
            showError("Full name is required");
            return false;
        }
        if (name.length() < 2) {
            showError("Name must be at least 2 characters");
            return false;
        }
        if (email.isEmpty()) {
            showError("Email address is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email format (e.g., maria@example.com)");
            return false;
        }
        if (phone.isEmpty() || phone.length() < 10) {
            showError("Phone number must be at least 10 digits");
            return false;
        }
        if (birthday.isEmpty() || birthday.length() != 8) {
            showError("Birthday must be complete (MM/DD/YYYY)");
            return false;
        }
        if (gender.equals("Select Gender")) {
            showError("Please select a gender");
            return false;
        }
        if (address.isEmpty()) {
            showError("Address is required");
            return false;
        }
        if (address.length() < 5) {
            showError("Address must be at least 5 characters");
            return false;
        }
        if (password.isEmpty()) {
            showError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return false;
        }
        if (confirmPassword.isEmpty()) {
            showError("Please confirm your password");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }
        return true;
    }
}