package com.mealmatch.data.network.service

import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.MatchResultResponse
import com.mealmatch.data.model.MatchSessionResponse
import com.mealmatch.data.model.SubmitSwipesRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionApiService {
    @POST("api/sessions/group/{groupId}")
    suspend fun createMatchSession(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: String
    ): Response<ApiResponse<MatchSessionResponse>>

    @POST("api/sessions/solo")
    suspend fun createSoloSession(
        @Header("Authorization") token: String
    ): Response<ApiResponse<MatchSessionResponse>>

    @POST("api/sessions/swipes/{sessionId}")
    suspend fun submitSwipes(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String,
        @Body request: SubmitSwipesRequest
    ): Response<ApiResponse<Unit>>

    @GET("api/sessions/result/{sessionId}")
    suspend fun getSessionResult(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String
    ): Response<ApiResponse<MatchResultResponse>>

    @GET("api/sessions/group/{groupId}/active")
    suspend fun getActiveSessions(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: String
    ): Response<ApiResponse<List<MatchSessionResponse>>>
}