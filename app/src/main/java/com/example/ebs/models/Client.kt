package com.example.ebs.models

data class Client(
    val id: Int,
    val name: String,
    val stallNumber: String,
    val address: String,
    val meter: Meter?,
    val meterBalance: Double,
    val billings: List<Billing>,
    val consumptionData: List<ConsumptionData>
)