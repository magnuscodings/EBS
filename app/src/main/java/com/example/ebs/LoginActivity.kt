package com.example.ebs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.ebs.databinding.ActivityLoginBinding
import com.example.ebs.viewModels.AuthViewModel
import com.example.ebs.models.Role
import com.example.ebs.models.User
import com.example.ebs.viewModels.LoginState

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateInput(email, password)) {
                showLoading(true)
                viewModel.login(email, password)
            }
        }

        binding.btnForgot.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)  // Start the ForgotPassword activity
        }

    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            showLoading(false)
            result.onSuccess { loginState ->
                when (loginState) {
                    is LoginState.FirstTimeLogin -> {
                        showChangePasswordDialog()
                    }
                    is LoginState.RegularLogin -> {
                        navigateBasedOnRole(loginState.user)
                    }
                }
            }.onFailure { exception ->
                Toast.makeText(this, "Login failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialog = ChangePasswordDialog()
        dialog.show(supportFragmentManager, "change_password_dialog")
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Clear previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Validate email
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        }
//        else if (password.length < 8) {
//            binding.tilPassword.error = "Password must be at least 8 characters"
//            isValid = false
//        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
    }

    private fun navigateBasedOnRole(user: User) {
        val intent = when (user.role?.slug) {
            Role.CLIENT_SLUG -> Intent(this, ClientActivity::class.java)
            Role.READER_SLUG -> Intent(this, ReaderActivity::class.java)
            else -> {
                Toast.makeText(this, "Invalid role", Toast.LENGTH_LONG).show()
                return
            }
        }
        startActivity(intent)
        finish()
    }
}