package com.example.ebs.managers

import android.content.Context

class SessionManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("USER_TOKEN", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("USER_TOKEN", null)
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}