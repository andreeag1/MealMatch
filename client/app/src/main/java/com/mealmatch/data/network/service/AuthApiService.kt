package com.mealmatch.data.network.service

import com.mealmatch.data.model.LoginRequest
import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.AuthResponse
import com.mealmatch.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>
}