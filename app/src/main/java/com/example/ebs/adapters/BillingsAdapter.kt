package com.example.ebs.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ebs.databinding.ClientBillingLayoutBinding
import com.example.ebs.models.Billing

class BillingsAdapter : RecyclerView.Adapter<BillingsAdapter.BillingViewHolder>() {

    private var billings: List<Billing> = listOf()

    fun submitList(newBillings: List<Billing>) {
        billings = newBillings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingViewHolder {
        val binding = ClientBillingLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BillingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillingViewHolder, position: Int) {
        holder.bind(billings[position])
    }

    override fun getItemCount(): Int = billings.size

    class BillingViewHolder(
        private val binding: ClientBillingLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(billing: Billing) {
            binding.apply {
                tvMeterCode.text = billing.meterCode
                tvReading.text = billing.reading
                tvDateOfReading.text = billing.dateOfReading
                tvConsumption.text = billing.consumption
                tvRate.text = billing.rate
                tvTotalAmount.text = billing.totalAmount
                tvBillingDate.text = billing.billingDate

                // Set status text
                tvStatus.text = if (billing.status == "1") "Paid" else "Unpaid"

                // Set background color using ColorDrawable
                val backgroundColor = when (billing.status.lowercase()) {
                    "1" -> Color.parseColor("#4CAF50")  // Material Green
                    else -> Color.parseColor("#F44336")    // Material Red
                }

                tvStatus.setBackgroundColor(backgroundColor)
                // Set text color to white for better contrast
                tvStatus.setTextColor(Color.WHITE)

                // Add padding to the status TextView for better appearance
                tvStatus.setPadding(16, 8, 16, 8)
            }
        }
    }

}