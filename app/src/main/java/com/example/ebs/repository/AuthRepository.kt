package com.example.ebs.repository

import android.util.Log
import com.example.ebs.NetworkUtils
import com.example.ebs.interfaces.ApiService
import com.example.ebs.models.User
import com.example.ebs.requests.ForgotRequest
import com.example.ebs.requests.LoginRequest
import com.example.ebs.responses.ApiResponse
import com.example.ebs.responses.ForgotResponse
import com.example.ebs.responses.LoginResponse
import org.json.JSONObject

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body()?.success == true) {
                val loginData = response.body()?.data
                if (loginData != null) {
                    // Use the token and is_first_login from the response
                    Result.success(loginData)
                } else {
                    Result.failure(Exception("Login data is null"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Login failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    suspend fun forgot(email: String): Result<ForgotResponse> {
//        return try {
//            val response = apiService.forgot(ForgotRequest(email))
//
//            if (response.isSuccessful && response.body()?.success == true) {
//                val loginData = response.body()?.data
//                if (loginData != null) {
//                    // Use the token and is_first_login from the response
//                    Result.success(loginData)
//
//                } else {
//                    Result.failure(Exception("Login data is null"))
//                }
//            } else {
//                val errorMessage = response.body()?.message ?: "Login failed"
//                Result.failure(Exception(errorMessage))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }



//    suspend fun forgot(email: String): String {
//        return try {
//            val response = apiService.forgot(ForgotRequest(email))
//            if (response.isSuccessful) {
//                response.body()?.message ?: "Unknown error: Response body is null"
//            } else {
////                "Error: ${response.errorBody()?.string() ?: "Unknown error"}"
//                response.body()?.message ?: "Unknown error: Response body is null"
//            }
//        } catch (e: Exception) {
//            "Exception: ${e.message}"
//        }
//    }

//    suspend fun forgot(email: String): String {
//        return try {
//            val response = apiService.forgot(ForgotRequest(email))
//            if (response.isSuccessful) {
//                response.body()?.message ?: "Unknown error: Response body is null"
//            } else {
//                // Extract the error message properly from errorBody()
//                val errorResponse = response.errorBody()?.string()
//                errorResponse ?: "Unknown error: No error message received"
//            }
//        } catch (e: Exception) {
//            "Exception: ${e.message}"
//        }
//    }

    suspend fun forgot(email: String): String {
        return try {
            val response = apiService.forgot(ForgotRequest(email))
            if (response.isSuccessful) {
                response.body()?.message ?: "Unknown error: Response body is null"
            } else {
                // Extract error message from errorBody()
                val errorResponse = response.errorBody()?.string()
                JSONObject(errorResponse).optString("message", "Unknown error") // Parse JSON safely
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }





    suspend fun getCurrentUser(token: String): Result<User> {
        return try {
            val response = apiService.getCurrentUser("Bearer $token")
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to get user data"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get user data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(fcmToken: String): Result<Unit> {
        return try {
            // Ensure the token is not null and not empty
            val sessionManager = NetworkUtils.getSessionManager()
            val userToken = sessionManager.getToken()

            if (userToken.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token found"))
            }

            // Create a map for the request body
            val requestBody = mapOf("fcmToken" to fcmToken)

            val response = apiService.updateFcmToken("Bearer $userToken", requestBody)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update FCM token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}