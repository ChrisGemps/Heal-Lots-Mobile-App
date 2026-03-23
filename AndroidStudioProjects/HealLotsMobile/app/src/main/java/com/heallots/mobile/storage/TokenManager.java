package com.heallots.mobile.storage;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.heallots.mobile.models.User;
import com.google.gson.Gson;

public class TokenManager {
    private static final String PREFS_NAME = "HealLotsPrefs";
    private static final String TOKEN_KEY = "token";
    private static final String USER_KEY = "user";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public TokenManager(Context context) {
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            this.sharedPreferences = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKey,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback to regular SharedPreferences if encryption fails
            this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        this.gson = new Gson();
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        sharedPreferences.edit().putString(USER_KEY, userJson).apply();
    }

    public User getUser() {
        String userJson = sharedPreferences.getString(USER_KEY, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void logout() {
        sharedPreferences.edit().clear().apply();
    }

    public String getAuthorizationHeader() {
        String token = getToken();
        if (token != null) {
            return "Bearer " + token;
        }
        return null;
    }
}
