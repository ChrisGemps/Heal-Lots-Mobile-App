package com.heallots.mobile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.heallots.mobile.features.appointments.dashboard.DashboardActivity
import com.heallots.mobile.features.auth.login.LoginActivity
import com.heallots.mobile.storage.TokenManager

class MainActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate started")

        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "activity_main layout inflated")

            tokenManager = TokenManager(this)
            Log.d(TAG, "TokenManager initialized")

            Handler(Looper.getMainLooper()).postDelayed({
                Log.d(TAG, "Splash screen timeout reached, checking login state")
                try {
                    val isLoggedIn = tokenManager.isLoggedIn()
                    Log.d(TAG, "isLoggedIn: $isLoggedIn")

                    if (isLoggedIn) {
                        Log.d(TAG, "User is logged in, navigating to DashboardActivity")
                        startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                    } else {
                        Log.d(TAG, "User is not logged in, navigating to LoginActivity")
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    }
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during navigation: ${e.message}", e)
                    try {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    } catch (e2: Exception) {
                        Log.e(TAG, "Error starting LoginActivity: ${e2.message}", e2)
                    }
                }
            }, 2000)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            try {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } catch (e2: Exception) {
                Log.e(TAG, "Error in catch block: ${e2.message}", e2)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
