package com.example.ebs.interfaces

import com.example.ebs.models.ClientResponse
import com.example.ebs.models.Meter
import com.example.ebs.models.MeterReading
import com.example.ebs.models.User
import com.example.ebs.requests.ChangePasswordRequest
import com.example.ebs.requests.ForgotRequest
import com.example.ebs.requests.LoginRequest
import com.example.ebs.requests.StoreMeterReadingRequest
import com.example.ebs.responses.ApiResponse
import com.example.ebs.responses.BillingResponse
import com.example.ebs.responses.ForgotResponse
import com.example.ebs.responses.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ApiResponse<LoginResponse>>


    @POST("api/forgot")
    suspend fun forgot(@Body forgotRequest: ForgotRequest): Response<ApiResponse<ForgotResponse>>

    @POST("api/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("api/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/change-first-time-password")
    suspend fun changeFirstTimePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    @GET("api/user")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<ApiResponse<User>>

    @POST("api/update-fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @GET("api/meter")
    suspend fun getMeters(): Response<List<Meter>>

    @POST("api/meterReading")
    suspend fun submitReading(
        @Body reading: StoreMeterReadingRequest
    ): Response<ApiResponse<MeterReading>>

    @GET("api/meter/{meterId}/last-reading")
    suspend fun getPreviousReading(@Path("meterId") meterId: Int): Response<ApiResponse<MeterReading>>

//    @GET("api/billingNotification/{clientID}")
//    suspend fun getBillingStatus(@Path("clientID") clientID: String): Response<BillingResponse>

    @GET("api/billingNotification/{clientID}")
    suspend fun getBillingStatus(@Path("clientID") clientID: String): Response<BillingResponse>


    @GET("api/client/me")
    suspend fun getClientDetails(
        @Header("Authorization") token: String
    ): Response<ClientResponse>
}