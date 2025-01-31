package com.example.ebs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.ebs.databinding.DialogChangePasswordBinding
import com.example.ebs.viewModels.AuthViewModel

class ChangePasswordDialog: DialogFragment() {
    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        isCancelable = false
        return binding.root
    }

    private fun setupUI() {
        binding.btnChangePassword.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validateInput(newPassword, confirmPassword)) {
                showLoading(true)
                viewModel.changeFirstTimePassword(newPassword, confirmPassword)
            }
        }
    }

    private fun validateInput(newPassword: String, confirmPassword: String): Boolean {
        var isValid = true

        binding.tilNewPassword.error = null
        binding.tilConfirmPassword.error = null

        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "New password is required"
            isValid = false
        } else if (newPassword.length < 8) {
            binding.tilNewPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirm password is required"
            isValid = false
        } else if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnChangePassword.isEnabled = !show
        binding.etNewPassword.isEnabled = !show
        binding.etConfirmPassword.isEnabled = !show
    }

    private fun observeViewModel() {
        viewModel.passwordChangeResult.observe(viewLifecycleOwner) { result ->
            showLoading(false)
            result.onSuccess {
                dismiss()
                // The navigation will be handled by LoginActivity through loginResult
            }.onFailure { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to change password: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}