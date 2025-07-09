package com.mealmatch.data.network.service

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.FriendListResponse

import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Path

interface FriendApiService {
    @GET("api/friends/list")
    suspend fun getFriends(
    @Header("Authorization") token: String
    ): Response<FriendListResponse>

    @POST("api/friends/add")
    suspend fun addFriend(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @POST("api/friends/remove")
    suspend fun removeFriend(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>  // {"friendUsername": "lama"}
    ): Response<ApiResponse<Unit>>


}