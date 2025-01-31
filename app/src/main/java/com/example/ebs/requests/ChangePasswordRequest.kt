package com.example.ebs.requests

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirmation: String
)
