package com.example.ebs.responses

data class LoginResponse(
    val token: String,
    val isFirstLogin: Boolean
)
