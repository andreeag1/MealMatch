package com.mealmatch.data.network.repository

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.AuthResponse
import com.mealmatch.data.model.LoginRequest
import com.mealmatch.data.model.RegisterRequest
import com.mealmatch.data.network.ApiClient
import retrofit2.Response

class AuthRepository {
    private val authApiService = ApiClient.authApiService

    suspend fun login(request: LoginRequest): Response<ApiResponse<AuthResponse>> {
        return authApiService.login(request)
    }

    suspend fun register(request: RegisterRequest): Response<ApiResponse<AuthResponse>> {
        return authApiService.register(request)
    }
}