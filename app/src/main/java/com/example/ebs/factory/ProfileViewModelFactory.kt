package com.example.ebs.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ebs.MyApplication
import com.example.ebs.models.User
import com.example.ebs.viewModels.ProfileViewModel

class ProfileViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Assuming MyApplication has apiService and sessionManager instances
            return ProfileViewModel(
                MyApplication.apiService,
                MyApplication.sessionManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}