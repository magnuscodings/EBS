package com.example.ebs.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ebs.MyApplication
import com.example.ebs.viewModels.MeterReadingViewModel

class MeterReadingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeterReadingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeterReadingViewModel(MyApplication.apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}