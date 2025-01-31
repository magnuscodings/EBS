package com.example.ebs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ebs.databinding.ActivityForgotPasswordBinding
import com.example.ebs.viewModels.AuthViewModel
import kotlinx.coroutines.launch

class ForgotPassword : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Use binding.root instead of setContentView(R.layout.activity_forgot_password)

        binding.btnSubmitEmail.setOnClickListener {
            val email = binding.etEmail.text.toString().trim() // Trim whitespace
            viewModel.forgotPassword(email, it.context)
            Log.d("Email", email) // Log the actual email

        }


    }
}
