package com.mealmatch.data.model

data class FriendRequest(
    val _id: String,
    val fromUser: FriendModel,
    val toUser: FriendModel,
    val status: String // "pending", "accepted", "declined"
)

data class FriendRequestsResponse(
    val success: Boolean,
    val data: List<FriendRequest>
)