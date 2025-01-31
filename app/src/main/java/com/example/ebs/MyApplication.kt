package com.example.ebs

import android.app.Application
import com.example.ebs.interfaces.ApiService
import com.example.ebs.managers.SessionManager
import com.example.ebs.repository.AuthRepository
import com.google.firebase.FirebaseApp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit



class MyApplication: Application() {
    companion object {
        private var _apiService: ApiService? = null
        private var _authRepository: AuthRepository? = null
        private var _sessionManager: SessionManager? = null

        val apiService: ApiService get() = _apiService ?: throw IllegalStateException("ApiService not initialized")
        val authRepository: AuthRepository get() = _authRepository ?: throw IllegalStateException("AuthRepository not initialized")
        val sessionManager: SessionManager get() = _sessionManager ?: throw IllegalStateException("SessionManager not initialized")
    }

    override fun onCreate() {
        super.onCreate()

// Add logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log full request & response body
        }


        // Initialize SessionManager first
        _sessionManager = SessionManager(applicationContext)


        // Create OkHttpClient with interceptor for tokens
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Logs API calls in Logcat
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val token = _sessionManager?.getToken()

                // If token exists, add it to header
                val request = if (token != null) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }

                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // For local development with Android Emulator
//            .baseUrl("http://127.0.0.1:8000/") // For local development with Android Emulator
            //.baseUrl("http://ipaddress-pc:port/") // For physical device
            //.baseUrl("https://your-production-server.com/") // For production
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create API service
        _apiService = retrofit.create(ApiService::class.java)

        // Initialize repository
        _authRepository = AuthRepository(_apiService!!)

        //Initialize pusher
        FirebaseApp.initializeApp(this)
    }


}