package com.example.ebs

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ebs.databinding.ActivityProfileBinding
import com.example.ebs.factory.ProfileViewModelFactory
import com.example.ebs.viewModels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupListeners()

        viewModel.loadUserProfile()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Handles back navigation
        }
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            binding.apply {
                userName.text = user.name
                userEmail.text = user.email
                userRole.text = user.role?.name
            }
        }

        viewModel.passwordChangeStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { status ->
                when (status) {
                    is ProfileViewModel.PasswordChangeStatus.Success -> {
                        showSuccess("Password changed successfully")
                        binding.changePasswordLayout.visibility = View.GONE
                    }
                    is ProfileViewModel.PasswordChangeStatus.Error -> {
                        showError(status.message)
                    }
                }
            }
        }

        viewModel.logoutEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        // Create login intent
        val intent = Intent(this, LoginActivity::class.java).apply {
            // Clear all activities on top and start fresh
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setupListeners() {
        binding.apply {
            changePasswordButton.setOnClickListener {
                changePasswordLayout.visibility = View.VISIBLE
            }

            submitPasswordButton.setOnClickListener {
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                when {
                    currentPassword.isEmpty() -> {
                        currentPasswordLayout.error = "Current password is required"
                    }
                    newPassword.isEmpty() -> {
                        newPasswordLayout.error = "New password is required"
                    }
                    newPassword.length < 8 -> {
                        newPasswordLayout.error = "Password must be at least 8 characters"
                    }
                    newPassword != confirmPassword -> {
                        confirmPasswordLayout.error = "Passwords do not match"
                    }
                    else -> {
                        clearErrors()
                        viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                    }
                }
            }

            logoutButton.setOnClickListener {
                showLogoutConfirmationDialog()
            }
        }
    }

    private fun clearErrors() {
        binding.apply {
            currentPasswordLayout.error = null
            newPasswordLayout.error = null
            confirmPasswordLayout.error = null
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.success))
            .show()
    }

}