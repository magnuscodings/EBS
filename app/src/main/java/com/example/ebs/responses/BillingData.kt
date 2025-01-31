package com.example.ebs.responses

data class BillingData(
    val id: Int,
    val meterReadingId: Int,
    val clientId: Int,
    val rate: String,
    val totalAmount: String,
    val billingDate: String,
    val status: Int,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
    val paymentDate: String?
)

data class BillingResponse(
    val count: Int,
    val data: List<BillingData> // ✅ Ensure this matches `BillingData`
)
