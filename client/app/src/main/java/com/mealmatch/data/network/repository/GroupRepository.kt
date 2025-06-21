package com.mealmatch.data.network.repository

import com.mealmatch.data.model.CreateGroupRequest
import com.mealmatch.data.model.CreateGroupResponse
import com.mealmatch.data.model.GetGroupMessagesResponse
import com.mealmatch.data.network.ApiClient
import retrofit2.Response

class GroupRepository {
    private val groupApiService = ApiClient.groupApiService

    suspend fun createGroup(token: String, request: CreateGroupRequest): Response<CreateGroupResponse> {
        return groupApiService.createGroup(token, request)
    }

    suspend fun getGroupMessages(token: String, roomId: String): Response<List<GetGroupMessagesResponse>> {
        return groupApiService.getGroupMessages(token, roomId)
    }
}