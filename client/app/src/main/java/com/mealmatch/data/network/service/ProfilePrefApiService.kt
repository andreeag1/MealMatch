package com.mealmatch.data.network.repository

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.UserProfileMessage
import com.mealmatch.data.network.ApiClient
import retrofit2.Response

class ProfilePrefApiService {
    private val profilePrefApiService = ApiClient.profilePrefApiService

    suspend fun createProfilePref(token: String, request: UserProfileMessage): Response<ApiResponse<Unit>> {
        return profilePrefApiService.createProfilePref(token, request)
    }

    suspend fun getProfilePref(token: String): Response<ApiResponse<UserProfileMessage>> {
        return profilePrefApiService.getProfilePref(token)
    }
}