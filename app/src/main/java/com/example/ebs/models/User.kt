package com.example.ebs.models

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: Role?
)
