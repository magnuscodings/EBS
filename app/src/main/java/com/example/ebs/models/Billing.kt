package com.example.ebs.models

data class Billing(
    val meterCode: String,
    val reading: String,
    val dateOfReading: String,
    val consumption: String,
    val rate: String,
    val totalAmount: String,
    val billingDate: String,
    val status: String
)