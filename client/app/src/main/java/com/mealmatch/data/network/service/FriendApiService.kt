package com.mealmatch.data.network.service

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.FriendListResponse
import com.mealmatch.data.model.FriendRequestsResponse

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
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @POST("api/friend-requests")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    @GET("api/friend-requests")
    suspend fun getFriendRequests(
        @Header("Authorization") token: String,
        @Query("type") type: String
    ): Response<FriendRequestsResponse>

    @POST("api/friend-requests/accept")
    suspend fun acceptFriendRequest(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    @POST("api/friend-requests/decline")
    suspend fun declineFriendRequest(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<Unit>

}