package com.heallots.mobile.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.heallots.mobile.R;
import com.heallots.mobile.api.ApiClient;
import com.heallots.mobile.api.ApiService;
import com.heallots.mobile.models.User;
import com.heallots.mobile.storage.TokenManager;
import com.heallots.mobile.utils.Constants;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private final ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleSelectedPhoto
    );

    private TokenManager tokenManager;
    private ApiService apiService;
    private User currentUser;

    private Button backBtn;
    private Button signOutBtn;
    private Button editBtn;
    private Button saveBtn;
    private Button changePasswordBtn;
    private Button updatePasswordBtn;
    private Button cancelPasswordBtn;
    private ImageView photoImage;
    private TextView photoInitial;
    private Button changePhotoBtn;
    private TextView photoError;
    private TextView profileNameDisplay;
    private TextView fullNameView;
    private TextView emailView;
    private TextView phoneView;
    private TextView birthdayView;
    private TextView addressView;
    private TextView genderView;
    private EditText fullNameEdit;
    private EditText emailEdit;
    private EditText phoneEdit;
    private EditText birthdayEdit;
    private EditText addressEdit;
    private Spinner genderSpinner;
    private LinearLayout passwordViewState;
    private LinearLayout passwordEditState;
    private EditText currentPasswordEdit;
    private EditText newPasswordEdit;
    private EditText confirmPasswordEdit;
    private ImageView currentPasswordToggle;
    private ImageView newPasswordToggle;
    private ImageView confirmPasswordToggle;
    private ProgressBar passwordStrengthBar;
    private TextView passwordStrengthText;
    private TextView passwordMatchText;
    private FrameLayout successBanner;
    private FrameLayout errorBanner;
    private TextView successMessage;
    private TextView errorMessage;

    private boolean isEditingProfile = false;
    private boolean isEditingPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_profile);
            tokenManager = new TokenManager(this);
            apiService = ApiClient.getApiService();
            currentUser = tokenManager.getUser();

            initializeViews();
            setupGenderSpinner();
            bindUserInfo();
            setupListeners();
            setupPasswordListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = tokenManager.getUser();
        bindUserInfo();
    }

    private void initializeViews() {
        backBtn = findViewById(R.id.profileBackBtn);
        signOutBtn = findViewById(R.id.profileSignOutBtn);
        editBtn = findViewById(R.id.profileEditBtn);
        saveBtn = findViewById(R.id.profileSaveBtn);
        changePasswordBtn = findViewById(R.id.profileChangePasswordBtn);
        updatePasswordBtn = findViewById(R.id.profileUpdatePasswordBtn);
        cancelPasswordBtn = findViewById(R.id.profileCancelPasswordBtn);
        photoImage = findViewById(R.id.profilePhotoImage);
        photoInitial = findViewById(R.id.profilePhotoInitial);
        changePhotoBtn = findViewById(R.id.profileChangePhotoBtn);
        photoError = findViewById(R.id.profilePhotoError);
        profileNameDisplay = findViewById(R.id.profileNameDisplay);
        fullNameView = findViewById(R.id.profileFullNameView);
        emailView = findViewById(R.id.profileEmailView);
        phoneView = findViewById(R.id.profilePhoneView);
        birthdayView = findViewById(R.id.profileBirthdayView);
        addressView = findViewById(R.id.profileAddressView);
        genderView = findViewById(R.id.profileGenderView);
        fullNameEdit = findViewById(R.id.profileFullNameEdit);
        emailEdit = findViewById(R.id.profileEmailEdit);
        phoneEdit = findViewById(R.id.profilePhoneEdit);
        birthdayEdit = findViewById(R.id.profileBirthdayEdit);
        addressEdit = findViewById(R.id.profileAddressEdit);
        genderSpinner = findViewById(R.id.profileGenderEdit);
        passwordViewState = findViewById(R.id.passwordViewState);
        passwordEditState = findViewById(R.id.passwordEditState);
        currentPasswordEdit = findViewById(R.id.profileCurrentPasswordEdit);
        newPasswordEdit = findViewById(R.id.profileNewPasswordEdit);
        confirmPasswordEdit = findViewById(R.id.profileConfirmPasswordEdit);
        currentPasswordToggle = findViewById(R.id.profileCurrentPasswordToggle);
        newPasswordToggle = findViewById(R.id.profileNewPasswordToggle);
        confirmPasswordToggle = findViewById(R.id.profileConfirmPasswordToggle);
        passwordStrengthBar = findViewById(R.id.profilePasswordStrengthBar);
        passwordStrengthText = findViewById(R.id.profilePasswordStrengthText);
        passwordMatchText = findViewById(R.id.profilePasswordMatchText);
        successBanner = findViewById(R.id.profileSuccessBanner);
        errorBanner = findViewById(R.id.profileErrorBanner);
        successMessage = findViewById(R.id.profileSuccessMessage);
        errorMessage = findViewById(R.id.profileErrorMessage);
    }

    private void setupGenderSpinner() {
        String[] genderOptions = {"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }

    private void bindUserInfo() {
        if (currentUser == null) {
            currentUser = new User();
            currentUser.setFullName(readPref("fullName", "User"));
            currentUser.setEmail(readPref("email", ""));
            currentUser.setPhone(readPref("phone", ""));
            currentUser.setBirthday(readPref("birthday", ""));
            currentUser.setAddress(readPref("address", ""));
            currentUser.setGender(readPref("gender", ""));
            currentUser.setProfilePictureUrl(readPref("profilePictureUrl", ""));
            currentUser.setRole(readPref("role", "USER"));
        }

        String fullName = safeText(currentUser.getFullName(), "User");
        profileNameDisplay.setText(fullName);
        fullNameView.setText(fullName);
        emailView.setText(safeText(currentUser.getEmail(), ""));
        phoneView.setText(formatPhoneForDisplay(currentUser.getPhone()));
        birthdayView.setText(formatBirthdayForDisplay(currentUser.getBirthday()));
        addressView.setText(safeText(currentUser.getAddress(), ""));
        genderView.setText(safeText(currentUser.getGender(), ""));

        fullNameEdit.setText(fullName);
        emailEdit.setText(safeText(currentUser.getEmail(), ""));
        phoneEdit.setText(formatPhoneForDisplay(currentUser.getPhone()));
        birthdayEdit.setText(formatBirthdayForDisplay(currentUser.getBirthday()));
        addressEdit.setText(safeText(currentUser.getAddress(), ""));
        setSpinnerValue(genderSpinner, safeText(currentUser.getGender(), "Prefer not to say"));

        photoInitial.setText(fullName.substring(0, 1).toUpperCase(Locale.US));
        loadProfilePhoto();
    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> finish());
        signOutBtn.setOnClickListener(v -> handleLogout());
        editBtn.setOnClickListener(v -> toggleEditMode());
        saveBtn.setOnClickListener(v -> saveProfileChanges());
        changePhotoBtn.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        changePasswordBtn.setOnClickListener(v -> togglePasswordEdit());
        updatePasswordBtn.setOnClickListener(v -> updatePassword());
        cancelPasswordBtn.setOnClickListener(v -> cancelPasswordEdit());
    }

    private void setupPasswordListeners() {
        currentPasswordToggle.setOnClickListener(v -> togglePasswordVisibility(currentPasswordEdit, currentPasswordToggle));
        newPasswordToggle.setOnClickListener(v -> togglePasswordVisibility(newPasswordEdit, newPasswordToggle));
        confirmPasswordToggle.setOnClickListener(v -> togglePasswordVisibility(confirmPasswordEdit, confirmPasswordToggle));

        newPasswordEdit.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
                checkPasswordMatch();
            }
        });

        confirmPasswordEdit.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordMatch();
            }
        });
    }

    private void toggleEditMode() {
        isEditingProfile = !isEditingProfile;

        int viewVisibility = isEditingProfile ? View.GONE : View.VISIBLE;
        int editVisibility = isEditingProfile ? View.VISIBLE : View.GONE;

        fullNameView.setVisibility(viewVisibility);
        emailView.setVisibility(viewVisibility);
        phoneView.setVisibility(viewVisibility);
        birthdayView.setVisibility(viewVisibility);
        addressView.setVisibility(viewVisibility);
        genderView.setVisibility(viewVisibility);

        fullNameEdit.setVisibility(editVisibility);
        emailEdit.setVisibility(editVisibility);
        phoneEdit.setVisibility(editVisibility);
        birthdayEdit.setVisibility(editVisibility);
        addressEdit.setVisibility(editVisibility);
        genderSpinner.setVisibility(editVisibility);

        saveBtn.setVisibility(isEditingProfile ? View.VISIBLE : View.GONE);
        editBtn.setText(isEditingProfile ? "Cancel" : "Edit");

        if (!isEditingProfile) {
            bindUserInfo();
        }
    }

    private void saveProfileChanges() {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            showError("Please sign in again to update your profile");
            return;
        }

        String fullName = fullNameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String phone = normalizePhoneForApi(phoneEdit.getText().toString().trim());
        String birthday = normalizeBirthdayForApi(birthdayEdit.getText().toString().trim());
        String address = addressEdit.getText().toString().trim();
        String gender = String.valueOf(genderSpinner.getSelectedItem());

        if (fullName.isEmpty() || email.isEmpty()) {
            showError("Full Name and Email are required");
            return;
        }

        User requestUser = new User();
        requestUser.setFullName(fullName);
        requestUser.setEmail(email);
        requestUser.setPhone(phone);
        requestUser.setBirthday(birthday);
        requestUser.setAddress(address);
        requestUser.setGender(gender);
        requestUser.setProfilePictureUrl(currentUser != null ? currentUser.getProfilePictureUrl() : null);

        saveBtn.setEnabled(false);
        apiService.updateProfile(authHeader, requestUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                saveBtn.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Failed to save profile changes");
                    return;
                }

                currentUser = response.body();
                persistUser(currentUser);
                bindUserInfo();
                if (isEditingProfile) {
                    toggleEditMode();
                }
                showSuccess("Profile updated successfully!");
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                saveBtn.setEnabled(true);
                Log.e(TAG, "Failed to update profile", t);
                showError("Failed to save profile changes");
            }
        });
    }

    private void togglePasswordEdit() {
        isEditingPassword = !isEditingPassword;
        passwordViewState.setVisibility(isEditingPassword ? View.GONE : View.VISIBLE);
        passwordEditState.setVisibility(isEditingPassword ? View.VISIBLE : View.GONE);
        clearPasswordFields();
    }

    private void updatePassword() {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            showError("Please sign in again to update your password");
            return;
        }

        String currentPassword = currentPasswordEdit.getText().toString();
        String newPassword = newPasswordEdit.getText().toString();
        String confirmPassword = confirmPasswordEdit.getText().toString();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("All password fields are required");
            return;
        }
        if (newPassword.length() < 8) {
            showError("New password must be at least 8 characters");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", currentPassword);
        request.put("newPassword", newPassword);

        updatePasswordBtn.setEnabled(false);
        apiService.changePassword(authHeader, request).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                updatePasswordBtn.setEnabled(true);
                if (!response.isSuccessful()) {
                    showError("Failed to update password");
                    return;
                }
                showSuccess("Password updated successfully!");
                cancelPasswordEdit();
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                updatePasswordBtn.setEnabled(true);
                Log.e(TAG, "Failed to update password", t);
                showError("Failed to update password");
            }
        });
    }

    private void cancelPasswordEdit() {
        clearPasswordFields();
        isEditingPassword = false;
        passwordViewState.setVisibility(View.VISIBLE);
        passwordEditState.setVisibility(View.GONE);
    }

    private void handleSelectedPhoto(Uri uri) {
        if (uri == null) {
            return;
        }

        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            showError("Please sign in again to upload a photo");
            return;
        }

        try {
            byte[] fileBytes = readBytes(uri);
            if (fileBytes == null || fileBytes.length == 0) {
                showError("Unable to read the selected image");
                return;
            }

            String mimeType = getContentResolver().getType(uri);
            RequestBody requestBody = RequestBody.create(
                    fileBytes,
                    MediaType.parse(mimeType != null ? mimeType : "image/*")
            );
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "profile-image.jpg", requestBody);

            apiService.uploadProfilePicture(authHeader, part).enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        showError("Failed to upload profile picture");
                        return;
                    }

                    currentUser = response.body();
                    persistUser(currentUser);
                    bindUserInfo();
                    showSuccess("Profile picture updated!");
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to upload profile picture", t);
                    showError("Failed to upload profile picture");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to process photo", e);
            showError("Failed to open selected image");
        }
    }

    private byte[] readBytes(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }

    private void loadProfilePhoto() {
        photoError.setVisibility(View.GONE);
        String profilePictureUrl = currentUser != null ? currentUser.getProfilePictureUrl() : null;
        if (profilePictureUrl == null || profilePictureUrl.trim().isEmpty()) {
            photoImage.setImageDrawable(null);
            photoImage.setVisibility(View.VISIBLE);
            photoInitial.setVisibility(View.VISIBLE);
            return;
        }

        photoImage.setVisibility(View.VISIBLE);
        photoInitial.setVisibility(View.VISIBLE);
        String imageUrl = profilePictureUrl.startsWith("http")
                ? profilePictureUrl
                : Constants.BASE_URL + "/api/user/profile-picture/" + profilePictureUrl;
        Picasso.get().load(imageUrl).fit().centerCrop().into(photoImage, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                photoInitial.setVisibility(View.GONE);
                photoError.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                photoImage.setImageDrawable(null);
                photoInitial.setVisibility(View.VISIBLE);
                photoError.setVisibility(View.VISIBLE);
                photoError.setText("Unable to load profile photo");
            }
        });
    }

    private void persistUser(User user) {
        tokenManager.saveUser(user);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("fullName", safeText(user.getFullName(), "User"))
                .putString("email", safeText(user.getEmail(), ""))
                .putString("phone", safeText(user.getPhone(), ""))
                .putString("birthday", safeText(user.getBirthday(), ""))
                .putString("gender", safeText(user.getGender(), ""))
                .putString("address", safeText(user.getAddress(), ""))
                .putString("profilePictureUrl", safeText(user.getProfilePictureUrl(), ""))
                .putString("role", safeText(user.getRole(), "USER"))
                .apply();
    }

    private void handleLogout() {
        tokenManager.logout();
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clearPasswordFields() {
        currentPasswordEdit.setText("");
        newPasswordEdit.setText("");
        confirmPasswordEdit.setText("");
        passwordStrengthBar.setProgress(0);
        passwordStrengthText.setText("");
        passwordMatchText.setText("");
    }

    private void updatePasswordStrength(String password) {
        int strength = 0;
        String strengthText = "Weak";
        int color = Color.RED;

        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[^A-Za-z0-9].*")) strength++;

        switch (strength) {
            case 2:
                strengthText = "Fair";
                color = Color.parseColor("#FF9800");
                break;
            case 3:
                strengthText = "Good";
                color = Color.parseColor("#FFC107");
                break;
            case 4:
                strengthText = "Strong";
                color = Color.parseColor("#4CAF50");
                break;
            case 5:
                strengthText = "Very Strong";
                color = Color.parseColor("#2196F3");
                break;
        }

        passwordStrengthBar.setProgress(strength);
        passwordStrengthText.setText(strengthText);
        passwordStrengthText.setTextColor(color);
    }

    private void checkPasswordMatch() {
        String newPass = newPasswordEdit.getText().toString();
        String confirmPass = confirmPasswordEdit.getText().toString();

        if (confirmPass.isEmpty()) {
            passwordMatchText.setText("");
            return;
        }

        if (newPass.equals(confirmPass)) {
            passwordMatchText.setText("Passwords match");
            passwordMatchText.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            passwordMatchText.setText("Passwords do not match");
            passwordMatchText.setTextColor(Color.RED);
        }
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon) {
        if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        toggleIcon.setImageResource(R.drawable.ic_placeholder);
        editText.setSelection(editText.getText().length());
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        if (adapter == null) {
            return;
        }
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equalsIgnoreCase(String.valueOf(adapter.getItem(i)))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private String readPref(String key, String fallback) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString(key, fallback);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String formatBirthdayForDisplay(String rawBirthday) {
        String value = safeText(rawBirthday, "");
        if (value.isEmpty()) {
            return "";
        }

        try {
            if (value.matches("\\d{8}")) {
                Date parsed = new SimpleDateFormat("MMddyyyy", Locale.US).parse(value);
                if (parsed != null) {
                    return new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(parsed);
                }
            }

            Date parsed = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(value);
            if (parsed != null) {
                return new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(parsed);
            }
        } catch (ParseException ignored) {
        }

        return value;
    }

    private String normalizeBirthdayForApi(String displayBirthday) {
        String value = safeText(displayBirthday, "");
        if (value.isEmpty()) {
            return "";
        }

        if (value.matches("\\d{8}")) {
            return value;
        }

        try {
            Date parsed = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(value);
            if (parsed != null) {
                return new SimpleDateFormat("MMddyyyy", Locale.US).format(parsed);
            }
        } catch (ParseException ignored) {
        }

        return value;
    }

    private String formatPhoneForDisplay(String rawPhone) {
        String value = safeText(rawPhone, "");
        if (value.isEmpty()) {
            return "";
        }

        if (value.contains(" ")) {
            return value;
        }

        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() == 11 && digits.startsWith("0")) {
            return digits.substring(0, 4) + " " + digits.substring(4, 7) + " " + digits.substring(7);
        }
        return value;
    }

    private String normalizePhoneForApi(String displayPhone) {
        return safeText(displayPhone, "").replaceAll("\\s+", "");
    }

    private void showSuccess(String message) {
        showPopupMessage(message);
    }

    private void showError(String message) {
        showPopupMessage(message);
    }

    private void showPopupMessage(String message) {
        if (successBanner != null) {
            successBanner.setVisibility(View.GONE);
        }
        if (errorBanner != null) {
            errorBanner.setVisibility(View.GONE);
        }
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
