package com.mealmatch.data.network.service

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.CreateGroupRequest
import com.mealmatch.data.model.GroupResponse
import com.mealmatch.data.model.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface GroupApiService {
    @POST("api/groups/")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body request: CreateGroupRequest
    ): Response<ApiResponse<Unit>>

    @GET("api/groups/")
    suspend fun getUserGroups(
        @Header("Authorization") token: String,
    ): Response<ApiResponse<List<GroupResponse>>>

    @GET("api/groups/{groupId}")
    suspend fun getGroupMessages(
        @Header("Authorization") token: String,
        @Path("groupId") roomId: String
    ): Response<ApiResponse<List<MessageResponse>>>
}