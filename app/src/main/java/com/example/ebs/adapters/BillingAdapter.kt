package com.example.ebs.adapters

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.ebs.R
import com.example.ebs.databinding.ClientBillingLayoutBinding
import com.example.ebs.models.Billing
import com.example.ebs.responses.BillingData
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BillingAdapter(private var billingList: List<BillingData>) : RecyclerView.Adapter<BillingAdapter.BillingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_billing, parent, false)
        return BillingViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: BillingViewHolder, position: Int) {
        val item = billingList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = billingList.size

    fun updateList(newList: List<BillingData>) {
        billingList = newList
        notifyDataSetChanged()
    }

    class BillingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(data: BillingData) {

            itemView.findViewById<TextView>(R.id.billing_rate).text = "Rate : " + data.rate
            itemView.findViewById<TextView>(R.id.billing_amount).text = "Amount : " + data.totalAmount

            val billingDateView = itemView.findViewById<TextView>(R.id.billing_date)
            val billingStatusView = itemView.findViewById<TextView>(R.id.billing_status)
            val dueTextView = itemView.findViewById<TextView>(R.id.isDueDate) // Due indicator TextView

            val isDue = isBillingDue(data.billingDate)

            billingDateView.text = formatDate(data.billingDate);


            if (data.status.equals("0") || data.paymentDate.isNullOrEmpty()) {  // Not Paid
                billingStatusView.text = "Not Paid"

                if (isDue) {
                    dueTextView.visibility = View.VISIBLE  // ✅ Show the due indicator
                    billingStatusView.setTextColor(Color.RED) // 🔴 Highlight overdue bills
                } else {
                    dueTextView.visibility = View.GONE  // ✅ Hide if not due
                    billingStatusView.setTextColor(Color.BLACK)
                }

            } else {  // Paid
                billingStatusView.text = "Paid"
                dueTextView.visibility = View.GONE  // ✅ Hide due indicator
                billingStatusView.setTextColor(Color.GREEN) // 🟢 Optional: Green for paid bills
            }






        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun isBillingDue(billingDate: String): Boolean {
            return try {
                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME // Match API format
                val billDate = LocalDate.parse(billingDate, formatter)
                val today = LocalDate.now()

                billDate.isBefore(today) // Return true if past due
            } catch (e: Exception) {
                false // In case of errors (e.g., wrong format)
            }
        }

        fun formatDate(isoDate: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Date only

                val date = inputFormat.parse(isoDate)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                "Invalid Date" // Handle parsing errors
            }
        }
    }
}
