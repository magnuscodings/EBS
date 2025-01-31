package com.example.ebs.responses

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
)