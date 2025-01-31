package com.example.ebs.viewModels

import android.content.Context
import android.net.Network
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebs.NetworkUtils
import com.example.ebs.models.User
import com.example.ebs.requests.ChangePasswordRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class LoginState {
    data class FirstTimeLogin(val user: User) : LoginState()
    data class RegularLogin(val user: User) : LoginState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = NetworkUtils.getAuthRepository()
    private val sessionManager = NetworkUtils.getSessionManager()
    private val apiService = NetworkUtils.getApiService()

    private val _loginResult = MutableLiveData<Result<LoginState>>()
    val loginResult: LiveData<Result<LoginState>> = _loginResult

    private val _isFirstLogin = MutableLiveData<Result<Boolean>>()
    val isFirstLogin: LiveData<Result<Boolean>> = _isFirstLogin

    private val _fcmResult = MutableLiveData<Result<String>>()
    val fcmResult: LiveData<Result<String>> = _fcmResult

    private val _passwordChangeResult = MutableLiveData<Result<Unit>>()
    val passwordChangeResult: LiveData<Result<Unit>> = _passwordChangeResult

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)

                result.onSuccess { loginResponse ->
                    sessionManager.saveToken(loginResponse.token)

                    val userResult = authRepository.getCurrentUser(loginResponse.token)
                    userResult.onSuccess { user ->
                        _user.value = user
                        val loginState = if (loginResponse.isFirstLogin) {
                            LoginState.FirstTimeLogin(user)
                        } else {
                            LoginState.RegularLogin(user)
                        }
                        _loginResult.value = Result.success(loginState)
                    }.onFailure { exception ->
                        _loginResult.value = Result.failure(exception)
                    }
                }.onFailure { exception ->
                    _loginResult.value = Result.failure(exception)
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }


    fun forgotPassword(email: String, context: Context) {
        viewModelScope.launch {
            val message = authRepository.forgot(email) // Call updated function

            withContext(Dispatchers.Main) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show() // Show message
                Log.d("ForgotTester", message) // Log message for debugging
            }
        }
    }



    fun changeFirstTimePassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                val response = apiService.changeFirstTimePassword(
                    ChangePasswordRequest(
                        currentPassword = "",
                        newPassword = newPassword,
                        newPasswordConfirmation = confirmPassword
                    )
                )

                if (response.isSuccessful) {
                    _user.value?.let { user ->
                        _passwordChangeResult.value = Result.success(Unit)
                        _loginResult.value = Result.success(LoginState.RegularLogin(user))
                    }
                } else {
                    _passwordChangeResult.value = Result.failure(
                        Exception(response.errorBody()?.string() ?: "Unknown error")
                    )
                }
            } catch (e: Exception) {
                _passwordChangeResult.value = Result.failure(e)
            }
        }
    }

    fun getFcmTokenAndUpdate() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                token?.let { fcmToken ->
                    viewModelScope.launch {
                        try {
                            val result = authRepository.updateFcmToken(
                                fcmToken
                            )
                            result.onSuccess {
                                _fcmResult.value = Result.success(token)
                                Log.d("FCM", "Token updated successfully")
                            }.onFailure { exception ->
                                // Log the specific error
                                Log.e("FCM", "Failed to update token: ${exception.message}")
                                _fcmResult.value = Result.failure(exception)
                            }
                        } catch (e: Exception) {
                            Log.e("FCM", "Error updating token: ${e.message}")
                            _fcmResult.value = Result.failure(e)
                        }
                    }
                }
            } else {
                Log.e("FCM", "Failed to get FCM token: ${task.exception?.message}")
            }
        }
    }

}