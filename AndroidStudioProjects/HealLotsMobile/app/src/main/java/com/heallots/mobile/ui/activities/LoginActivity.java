package com.heallots.mobile.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.heallots.mobile.R;
import com.heallots.mobile.api.ApiClient;
import com.heallots.mobile.api.ApiService;
import com.heallots.mobile.models.LoginRequest;
import com.heallots.mobile.models.LoginResponse;
import com.heallots.mobile.storage.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    private EditText emailEditText;
    private EditText passwordEditText;
    private ImageButton passwordToggleButton;
    private Button loginButton;
    private TextView registerLink;
    private FrameLayout errorBanner;
    private TextView errorMessage;
    
    private ApiService apiService;
    private TokenManager tokenManager;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);

            apiService = ApiClient.getApiService();
            tokenManager = new TokenManager(this);

            // Initialize views
            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);
            passwordToggleButton = findViewById(R.id.passwordToggleButton);
            loginButton = findViewById(R.id.loginButton);
            registerLink = findViewById(R.id.registerLink);
            errorBanner = findViewById(R.id.errorBanner);
            errorMessage = findViewById(R.id.errorMessage);

            if (emailEditText == null || passwordEditText == null || loginButton == null || registerLink == null) {
                Log.e(TAG, "One or more views are null!");
                finish();
                return;
            }

            // Setup password toggle
            passwordToggleButton.setOnClickListener(v -> togglePasswordVisibility());
            
            // Clear error on input
            emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) hideError();
            });
            passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) hideError();
            });

            // Setup button listeners
            loginButton.setOnClickListener(v -> handleLogin());
            registerLink.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        
        if (passwordVisible) {
            // Show password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggleButton.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            // Hide password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggleButton.setImageResource(android.R.drawable.ic_menu_view);
        }
        
        // Keep cursor at end
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorBanner.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorBanner.setVisibility(View.GONE);
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        // Hide keyboard
        hideKeyboard();

        // Disable button during login
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        hideError();

        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    tokenManager.saveToken(loginResponse.getToken());
                    tokenManager.saveUser(loginResponse.getUser());

                    // Navigate to dashboard based on role
                    Intent intent = loginResponse.getUser().getRole().equals("ADMIN")
                            ? new Intent(LoginActivity.this, AdminDashboardActivity.class)
                            : new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                    if (response.code() == 401) {
                        showError("Invalid email or password.");
                    } else {
                        showError("Login failed. Please try again.");
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Login error", t);
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
                showError("Server error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            return false;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            return false;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }

    private void hideKeyboard() {
        InputMethodManager imm = getSystemService(InputMethodManager.class);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
