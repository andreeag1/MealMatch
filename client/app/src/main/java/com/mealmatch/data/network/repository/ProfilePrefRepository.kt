package com.mealmatch.data.network.repository

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.UserPreferences
import com.mealmatch.data.network.ApiClient
import retrofit2.Response

class ProfilePrefRepository {
    private val profilePrefApiService = ApiClient.profilePrefApiService

    suspend fun setProfilePref(token: String, request: UserPreferences): Response<ApiResponse<Unit>> {
        return profilePrefApiService.setProfilePref(token, request)
    }

    suspend fun getProfilePref(token: String): Response<ApiResponse<UserPreferences>> {
        return profilePrefApiService.getProfilePref(token)
    }
}