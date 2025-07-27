package com.mealmatch.data.network.service

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.UserPreferences
import com.mealmatch.data.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ProfilePrefApiService {
    @POST("/api/user_profiles")
    suspend fun setProfilePref(
        @Header("Authorization") token: String,
        @Body request: UserPreferences
    ): Response<ApiResponse<Unit>>

    @GET("/api/user_profiles")
    suspend fun getProfilePref(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserPreferences>>

    @GET("api/users/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String,
    ): Response<ApiResponse<UserProfileResponse>>
}
