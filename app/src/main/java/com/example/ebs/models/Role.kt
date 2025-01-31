package com.example.ebs.models

data class Role(
    val id: Int,
    val name: String,
    val slug: String
) {
    companion object {
        const val CLIENT_SLUG = "client"
        const val READER_SLUG = "reader"
    }
}
