package com.example.ebs.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebs.interfaces.ApiService
import com.example.ebs.managers.SessionManager
import com.example.ebs.models.User
import com.example.ebs.requests.ChangePasswordRequest
import com.example.ebs.responses.Event
import kotlinx.coroutines.launch

class ProfileViewModel (
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _passwordChangeStatus = MutableLiveData<Event<PasswordChangeStatus>>()
    val passwordChangeStatus: LiveData<Event<PasswordChangeStatus>> = _passwordChangeStatus

    // Add a LiveData for logout event
    private val _logoutEvent = MutableLiveData<Event<Unit>>()
    val logoutEvent: LiveData<Event<Unit>> = _logoutEvent

    sealed class PasswordChangeStatus {
        object Success : PasswordChangeStatus()
        data class Error(val message: String) : PasswordChangeStatus()
    }

    fun loadUserProfile() {
        val token = sessionManager.getToken()
        Log.i("ProfileViewModel", "loadUserProfile: ${token.toString()}")
        viewModelScope.launch {
            try {
                val response = apiService.getCurrentUser(token.toString())
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        _user.value = it
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                val response = apiService.changePassword(
                    ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword,
                        newPasswordConfirmation = confirmPassword
                    )
                )

                if (response.isSuccessful) {
                    _passwordChangeStatus.value = Event(PasswordChangeStatus.Success)
                } else {
                    _passwordChangeStatus.value = Event(PasswordChangeStatus.Error("Failed to change password"))
                }
            } catch (e: Exception) {
                _passwordChangeStatus.value = Event(PasswordChangeStatus.Error(e.message ?: "Unknown error occurred"))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val token = sessionManager.getToken() // Use the instance variable
                token?.let { // Null check the token
                    apiService.logout("Bearer $it")
                }
            } catch (e: Exception) {
                // Even if the logout API fails, we'll still clear local session
            } finally {
                sessionManager.clearSession()
                _logoutEvent.value = Event(Unit)
            }
        }
    }


}