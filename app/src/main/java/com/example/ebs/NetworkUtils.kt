package com.example.ebs

import com.example.ebs.interfaces.ApiService
import com.example.ebs.managers.SessionManager
import com.example.ebs.repository.AuthRepository

object NetworkUtils {
    fun isInitialized(): Boolean {
        return try {
            MyApplication.apiService
            MyApplication.authRepository
            MyApplication.sessionManager
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    fun getApiService(): ApiService = MyApplication.apiService

    fun getAuthRepository(): AuthRepository = MyApplication.authRepository

    fun getSessionManager(): SessionManager = MyApplication.sessionManager
}