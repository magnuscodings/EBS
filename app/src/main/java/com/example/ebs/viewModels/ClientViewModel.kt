package com.example.ebs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebs.MyApplication.Companion.sessionManager
import com.example.ebs.NetworkUtils
import com.example.ebs.models.ClientResponse
import kotlinx.coroutines.launch


class ClientViewModel : ViewModel() {
    private val apiService = NetworkUtils.getApiService()
    private val _clientData = MutableLiveData<Result<ClientResponse>>()
    val clientData: LiveData<Result<ClientResponse>> = _clientData

    fun fetchClientData() {
        viewModelScope.launch {
            try {
                val token = sessionManager.getToken() ?: throw Exception("No token found")
                val response = apiService.getClientDetails("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let {
                        _clientData.value = Result.success(it)
                    } ?: throw Exception("Response body is null")
                } else {
                    throw Exception("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _clientData.value = Result.failure(e)
            }
        }
    }
}