package com.mealmatch.data.network.repository
import com.mealmatch.data.model.FriendListResponse
import com.mealmatch.data.network.ApiClient
import retrofit2.Response
import com.mealmatch.data.model.ApiResponse
import com.mealmatch.data.model.FriendRequestsResponse

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

    suspend fun sendFriendRequest(token: String, username: String): Response<Unit> {
        return api.sendFriendRequest(token, mapOf("username" to username))
    }

    suspend fun getFriendRequests(token: String, type: String): Response<FriendRequestsResponse> {
        return api.getFriendRequests(token, type)
    }

    suspend fun acceptFriendRequest(token: String, requestId: String): Response<Unit> {
        return api.acceptFriendRequest(token, mapOf("requestId" to requestId))
    }

    suspend fun declineFriendRequest(token: String, requestId: String): Response<Unit> {
        return api.declineFriendRequest(token, mapOf("requestId" to requestId))
    }

}