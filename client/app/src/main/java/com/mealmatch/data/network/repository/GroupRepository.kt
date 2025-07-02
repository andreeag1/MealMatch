package com.mealmatch.data.network.repository

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.CreateGroupRequest
import com.mealmatch.data.model.GroupResponse
import com.mealmatch.data.model.MessageResponse
import com.mealmatch.data.network.ApiClient
import retrofit2.Response

class GroupRepository {
    private val groupApiService = ApiClient.groupApiService

    suspend fun createGroup(token: String, request: CreateGroupRequest): Response<ApiResponse<Unit>> {
        return groupApiService.createGroup(token, request)
    }

    suspend fun getUserGroups(token: String): Response<ApiResponse<List<GroupResponse>>> {
        return groupApiService.getUserGroups(token)
    }

    suspend fun getGroupMessages(token: String, groupId: String): Response<ApiResponse<List<MessageResponse>>> {
        return groupApiService.getGroupMessages(token, groupId)
    }
}