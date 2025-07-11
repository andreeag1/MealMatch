package com.mealmatch.data.network.repository

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.MatchResultResponse
import com.mealmatch.data.model.MatchSessionResponse
import com.mealmatch.data.model.SubmitSwipesRequest
import com.mealmatch.data.network.ApiClient
import retrofit2.Response

class SessionRepository {
    private val sessionApiService = ApiClient.sessionApiService

    suspend fun createMatchSession(token: String, groupId: String): Response<ApiResponse<MatchSessionResponse>> {
        return sessionApiService.createMatchSession(token, groupId)
    }

    suspend fun createSoloSession(token: String): Response<ApiResponse<MatchSessionResponse>> {
        return sessionApiService.createSoloSession(token)
    }

    suspend fun submitSwipes(token: String, sessionId: String, request: SubmitSwipesRequest): Response<ApiResponse<Unit>> {
        return sessionApiService.submitSwipes(token, sessionId, request)
    }

    suspend fun getSessionResult(token: String, sessionId: String): Response<ApiResponse<MatchResultResponse>> {
        return sessionApiService.getSessionResult(token, sessionId)
    }

    suspend fun getActiveSessions(token: String, groupId: String): Response<ApiResponse<List<MatchSessionResponse>>> {
        return sessionApiService.getActiveSessions(token, groupId)
    }
}