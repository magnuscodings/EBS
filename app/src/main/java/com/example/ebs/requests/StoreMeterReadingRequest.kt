package com.example.ebs.requests

data class StoreMeterReadingRequest(
    val meterId: Int,
    val reading: Double,
)
