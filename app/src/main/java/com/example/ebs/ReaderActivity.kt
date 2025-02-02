package com.example.ebs

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ebs.MyApplication.Companion.sessionManager
import com.example.ebs.databinding.ActivityReaderBinding
import com.example.ebs.factory.MeterReadingViewModelFactory
import com.example.ebs.viewModels.MeterReadingViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ReaderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReaderBinding

    private val viewModel: MeterReadingViewModel by viewModels {
        MeterReadingViewModelFactory()
    }
    private var selectedMeterId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupListeners()

        viewModel.loadMeters()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide default title

        binding.btnProfile.setOnClickListener {
            // Navigate to login activity
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
//            finish()
        }
    }

    private fun setupObservers() {
        viewModel.meters.observe(this) { meters ->
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                meters.map { it.stallNumber }
            )
            binding.meterSpinner.setAdapter(adapter)
        }

        viewModel.submitStatus.observe(this) { status ->
            when (status) {
                is MeterReadingViewModel.SubmitStatus.Success -> {
                    showSuccess("Reading submitted successfully")
                    clearInputs()
                }
                is MeterReadingViewModel.SubmitStatus.Error -> {
                    showError(status.message)
                }
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.submitButton.isEnabled = !isLoading
            if (isLoading) {
                binding.submitButton.text = "Submitting..."
            } else {
                binding.submitButton.text = "Submit Reading"
            }
        }

        viewModel.previousReading.observe(this) { reading ->
            binding.previousReadingText.text = when (reading) {
                null -> "Previous reading: None"
                else -> "Previous reading: ${String.format("%.2f", reading)}"
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            meterSpinner.setOnItemClickListener { _, _, position, _ ->
                viewModel.meters.value?.let {
                    selectedMeterId = it[position].id
                    // Fetch previous reading when meter is selected
                    selectedMeterId?.let { meterId ->
                        viewModel.loadPreviousReading(meterId)
                        previousReadingText.text = "Previous reading: Fetching..."
                    }
                }
            }

            submitButton.setOnClickListener {
                val reading = readingInput.text.toString().toDoubleOrNull()
                val meterId = selectedMeterId

                when {
                    meterId == null -> {
                        showError("Please select a meter!")
                    }
                    reading == null -> {
                        readingInputLayout.error = "Please enter a valid reading"
                    }
                    else -> {
                        readingInputLayout.error = null
                        viewModel.submitReading(meterId, reading)
                    }
                }
            }

            // Clear error when user starts typing
            readingInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    readingInputLayout.error = null
                }
            }
        }
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

    private fun clearInputs() {
        binding.apply {
            readingInput.text?.clear()
            meterSpinner.text?.clear()
            readingInputLayout.error = null
            previousReadingText.text = "Previous reading: -"
        }
        selectedMeterId = null
    }

}