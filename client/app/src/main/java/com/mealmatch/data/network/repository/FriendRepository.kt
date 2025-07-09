package com.mealmatch.data.network.repository
import com.mealmatch.data.model.FriendListResponse
import com.mealmatch.data.network.ApiClient
import retrofit2.Response
import com.mealmatch.data.model.ApiResponse


class FriendRepository {
    private val api = ApiClient.friendApiService

    suspend fun getFriends(token: String): Response<FriendListResponse> {
        return api.getFriends(token)
    }

    // define response for both of these
    suspend fun addFriend(token: String, username: String): Response<ApiResponse<Unit>> {
        return api.addFriend(token, mapOf("friendUsername" to username))
    }

    suspend fun removeFriend(token: String, username: String): Response<ApiResponse<Unit>> {
        return api.removeFriend(token, mapOf("friendUsername" to username))

    }
}