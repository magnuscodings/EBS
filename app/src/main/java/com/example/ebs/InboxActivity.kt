package com.example.ebs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ebs.adapters.BillingAdapter
import com.example.ebs.databinding.ActivityInboxBinding
import com.example.ebs.viewModels.AuthViewModel

class InboxActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInboxBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var billingAdapter: BillingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView properly
        billingAdapter = BillingAdapter(emptyList())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InboxActivity)
            adapter = billingAdapter
        }
        binding.closeButton.setOnClickListener {
            finish() // Closes the activity
        }
        // Observe ViewModel LiveData and update the adapter
        viewModel.billingStatus.observe(this) { billingList ->
            billingAdapter.updateList(billingList)
        }

        // Fetch data
        viewModel.getRowsBillingStatus("1")
    }
}
