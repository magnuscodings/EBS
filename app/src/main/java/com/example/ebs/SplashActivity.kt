package com.example.ebs

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ebs.models.Role
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar if it exists
        supportActionBar?.hide()

        // Delayed navigation to check authentication and network initialization
        Handler(Looper.getMainLooper()).postDelayed({
            checkInitializationAndNavigate()
        }, 2000) // 2 seconds delay
    }

    private fun checkInitializationAndNavigate() {
        if (!NetworkUtils.isInitialized()) {
            // Handle initialization error
            Toast.makeText(this, "Error initializing network", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Get token and check if user is logged in
        val sessionManager = NetworkUtils.getSessionManager()
        val token = sessionManager.getToken()

        if (token != null) {
            // User is logged in, fetch their role
            fetchUserRoleAndNavigate(token)
        } else {
            // User is not logged in, go to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchUserRoleAndNavigate(token: String) {
        lifecycleScope.launch {
            try {
                val result = NetworkUtils.getAuthRepository().getCurrentUser(token)
                result.onSuccess { user ->
                    navigateBasedOnRole(user.role?.slug)
                }.onFailure { exception ->
                    // Token might be invalid or expired
                    Toast.makeText(this@SplashActivity, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
                    NetworkUtils.getSessionManager().clearSession()
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SplashActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun navigateBasedOnRole(roleSlug: String?) {
        val intent = when (roleSlug) {
            Role.CLIENT_SLUG -> Intent(this, ClientActivity::class.java)
            Role.READER_SLUG -> Intent(this, ReaderActivity::class.java)
            else -> {
                // Invalid role, clear session and go to login
                NetworkUtils.getSessionManager().clearSession()
                Intent(this, LoginActivity::class.java)
            }
        }
        startActivity(intent)
        finish()
    }

}