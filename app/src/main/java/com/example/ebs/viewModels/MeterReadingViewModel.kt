package com.example.ebs.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebs.MyApplication.Companion.sessionManager
import com.example.ebs.interfaces.ApiService
import com.example.ebs.models.Meter
import com.example.ebs.models.MeterReading
import com.example.ebs.requests.StoreMeterReadingRequest
import com.example.ebs.responses.ApiResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch

class MeterReadingViewModel (
    private val service: ApiService
) : ViewModel() {
    private val _meters = MutableLiveData<List<Meter>>()
    val meters: LiveData<List<Meter>> = _meters

    private val _previousReading = MutableLiveData<Double?>()
    val previousReading: LiveData<Double?> = _previousReading

    private val _submitStatus = MutableLiveData<SubmitStatus>()
    val submitStatus: LiveData<SubmitStatus> = _submitStatus

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    sealed class SubmitStatus {
        object Success : SubmitStatus()
        data class Error(val message: String) : SubmitStatus()
    }

    fun loadMeters() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = service.getMeters()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _meters.value = it
                    }
                } else {
                    _submitStatus.value = SubmitStatus.Error("Failed to load meters: ${response.message()}")
                }
            } catch (e: Exception) {
                _submitStatus.value = SubmitStatus.Error(e.message ?: "Unknown error occurred")
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadPreviousReading(meterId: Int) {
        viewModelScope.launch {
            try {
                val response = service.getPreviousReading(meterId)
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        _previousReading.value = it.reading
                    } ?: run {
                        _previousReading.value = null
                    }
                } else {
                    _previousReading.value = null
                    _submitStatus.value = SubmitStatus.Error("Failed to load previous reading")
                }
            } catch (e: Exception) {
                _previousReading.value = null
                _submitStatus.value = SubmitStatus.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun submitReading(meterId: Int, reading: Double) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val meterReading = StoreMeterReadingRequest(meterId, reading)
                val response = service.submitReading(meterReading)

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _submitStatus.value = SubmitStatus.Success
                        } else {
                            _submitStatus.value = SubmitStatus.Error(
                                apiResponse.message ?: "Failed to submit reading"
                            )
                        }
                    } ?: run {
                        _submitStatus.value = SubmitStatus.Error("Empty response from server")
                    }
                } else {
                    // Handle error response
                    val errorMessage = try {
                        // Use Gson to parse error response
                        val gson = GsonBuilder()
                            .setLenient()
                            .create()
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = gson.fromJson(errorBody, ApiResponse::class.java)
                        errorResponse?.message ?: "Failed to submit reading"
                    } catch (e: Exception) {
                        "Failed to submit reading: ${response.code()}"
                    }
                    _submitStatus.value = SubmitStatus.Error(errorMessage)
                }
            } catch (e: Exception) {
                _submitStatus.value = SubmitStatus.Error(e.message ?: "Unknown error occurred")
            } finally {
                _loading.value = false
            }
        }
    }
}