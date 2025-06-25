package com.mealmatch.data.network.repository

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.CreateGroupRequest
import com.mealmatch.data.model.GetGroupMessagesResponse
import com.mealmatch.data.model.GroupResponse
import com.mealmatch.data.network.ApiClient
import retrofit2.Response

class GroupRepository {
    private val groupApiService = ApiClient.groupApiService

    suspend fun createGroup(token: String, request: CreateGroupRequest): Response<ApiResponse<Unit>> {
        return groupApiService.createGroup(token, request)
    }

    suspend fun getGroupMessages(token: String, roomId: String): Response<ApiResponse<List<GetGroupMessagesResponse>>> {
        return groupApiService.getGroupMessages(token, roomId)
    }

    suspend fun getUserGroups(token: String): Response<ApiResponse<List<GroupResponse>>> {
        return groupApiService.getUserGroups(token)
    }
}