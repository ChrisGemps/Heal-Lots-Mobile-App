package com.heallots.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.heallots.mobile.storage.TokenManager;
import com.heallots.mobile.ui.activities.DashboardActivity;
import com.heallots.mobile.ui.activities.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate started");
        
        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "activity_main layout inflated");

            tokenManager = new TokenManager(this);
            Log.d(TAG, "TokenManager initialized");

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "Splash screen timeout reached, checking login state");
                try {
                    boolean isLoggedIn = tokenManager.isLoggedIn();
                    Log.d(TAG, "isLoggedIn: " + isLoggedIn);
                    
                    if (isLoggedIn) {
                        Log.d(TAG, "User is logged in, navigating to DashboardActivity");
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                    } else {
                        Log.d(TAG, "User is not logged in, navigating to LoginActivity");
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error during navigation: " + e.getMessage(), e);
                    // Default to login screen in case of error
                    try {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    } catch (Exception e2) {
                        Log.e(TAG, "Error starting LoginActivity: " + e2.getMessage(), e2);
                    }
                }
            }, 2000); // 2 second splash screen
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            // Fallback: go to login immediately
            try {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } catch (Exception e2) {
                Log.e(TAG, "Error in catch block: " + e2.getMessage(), e2);
            }
        }
    }
}
