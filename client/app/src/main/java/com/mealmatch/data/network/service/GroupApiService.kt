package com.mealmatch.data.network.service

import com.mealmatch.data.model.CreateGroupRequest
import com.mealmatch.data.model.CreateGroupResponse
import com.mealmatch.data.model.GetGroupMessagesResponse
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
    ): Response<CreateGroupResponse>

    @GET("api/groups/{roomId}")
    suspend fun getGroupMessages(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: String
    ): Response<List<GetGroupMessagesResponse>>
}