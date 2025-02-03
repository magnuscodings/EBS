package com.example.ebs

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ebs.adapters.BillingAdapter
import com.example.ebs.adapters.BillingsAdapter
import com.example.ebs.databinding.ActivityClientBinding
import com.example.ebs.models.Client
import com.example.ebs.models.ConsumptionData
import com.example.ebs.viewModels.AuthViewModel
import com.example.ebs.viewModels.ClientViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar

class ClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientBinding
    private val viewModel: ClientViewModel by viewModels()
    private val billingsAdapter = BillingsAdapter()
    private lateinit var lineChart: LineChart
    private lateinit var notificationService: NotificationService

    private val viewModelNotif: AuthViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)


        notif()
        setupToolbar()
        setupRecyclerView()
        setupChart()
        setupObservers()
        setupListeners()

        viewModel.fetchClientData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
//            finish()
        }

        binding.btnInbox.setOnClickListener {
            val intent = Intent(this, InboxActivity::class.java)
            startActivity(intent)
        }


    }

    private fun setupChart() {
        lineChart = binding.consumptionChart
        lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setDrawGridBackground(false)

            // Customize X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }

            // Customize Y-axis
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false

            // Enable zooming and scaling
            isDragEnabled = true
            setScaleEnabled(true)
        }
    }

    private fun setupListeners(){
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData(){
        viewModel.clientData.observe(this) { result ->
            result.onSuccess { response ->
                if (response.success) {
                    billingsAdapter.submitList(response.data.client.billings)
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }.onFailure {
                Snackbar.make(
                    binding.root,
                    "Failed to load client data: ${it.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }

        }
    }

    private fun setupRecyclerView() {
        binding.billingsRecyclerView.adapter = billingsAdapter
    }


    private fun setupObservers() {
        viewModel.clientData.observe(this) { result ->
            result.onSuccess { response ->
                if (response.success) {
                    updateUI(response.data.client)
                }
            }.onFailure {
                Snackbar.make(
                    binding.root,
                    "Failed to load client data: ${it.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateUI(client: Client) {
        binding.apply {
            tvClientName.text = client.name

            tvMeterCode.text = client.meter?.meterCode ?: "No meter assigned"

            tvMeterBalance.text = getString(R.string.peso_amount_format, client.meterBalance)

            updateConsumptionGraph(client.consumptionData)

            billingsAdapter.submitList(client.billings)
        }
    }

    private fun updateConsumptionGraph(consumptionData: List<ConsumptionData>) {
        val entries = consumptionData.mapIndexed { index, data ->
            Entry(index.toFloat(), data.consumption.toFloat())
        }

        val dataSet = LineDataSet(entries, "Monthly Consumption").apply {
            color = ContextCompat.getColor(this@ClientActivity, R.color.orange)
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(true)
            valueFormatter = DefaultValueFormatter(1)
        }

        lineChart.apply {
            data = LineData(dataSet)

            xAxis.valueFormatter = IndexAxisValueFormatter(
                consumptionData.map { it.month }
            )

            invalidate()
        }
    }


    private fun notif()
    {
        notificationService = NotificationService()

        var hasUnpaidBills = false
        var hasPaidBills = false


        viewModelNotif.billingStatus.observe(this) { billingList ->

            // Retrieve the processed billing IDs from SharedPreferences
            var processedBillingIds = getProcessedBillingIds()

            var counter = 0
            var hasUnpaidBills = false
            var hasPaidBills = false

            // Check each billing item to see if it's unpaid or paid
            billingList.forEach { billing ->
                val id = billing.id.toString()
                val status = billing.status.toString()

                Log.d("StatusChecker", "ID: $id, Status: $status")

                // Check if the bill has already been processed (notification sent previously)
                if (processedBillingIds.contains(id)) {
                    // Skip this bill as it has already been processed
                    return@forEach
                }

                // Check if there's at least one unpaid bill
                if (status == "0") {
                    counter += 1
                    hasUnpaidBills = true
                }

                // Check if there's at least one paid bill
                if (status == "1") {
                    hasPaidBills = true
                }

                // Mark this bill ID as processed and save it
                processedBillingIds.add(id)
            }

            // Save the updated set of processed IDs to SharedPreferences
            saveProcessedBillingIds(processedBillingIds)

            // Show a notification for unpaid bills if there are any
            if (hasUnpaidBills) {
                notificationService.showNotification(
                    this,
                    "Unpaid Bills ($counter)",
                    "You have $counter unpaid bills."
                )
            }

            // Show a notification for paid bills if there are any
            if (hasPaidBills) {
                notificationService.showNotification(
                    this,
                    "Paid Bills",
                    "You have paid your bill"
                )
            }
        }





        // Fetch data
        viewModelNotif.getRowsBillingStatus("1")



    }

    // Use SharedPreferences to store and retrieve processed billing IDs
    private fun getProcessedBillingIds(): MutableSet<String> {
        val sharedPreferences = getSharedPreferences("billing_preferences", MODE_PRIVATE)
        return sharedPreferences.getStringSet("processed_billing_ids", mutableSetOf()) ?: mutableSetOf()
    }

    private fun saveProcessedBillingIds(processedBillingIds: MutableSet<String>) {
        val sharedPreferences = getSharedPreferences("billing_preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("processed_billing_ids", processedBillingIds)
        editor.apply()
    }

}
