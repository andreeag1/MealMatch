package com.mealmatch.data.model

data class CreateGroupRequest(
    val name: String,
    val members: List<String>
)

data class CreateGroupResponse(
    val _id: String,
    val name: String,
    val members: List<String>,
    val createdAt: String,
    val updatedAt: String
)

data class GetGroupMessagesResponse(
    val _id: String,
    val room: String,
    val user: UserInfo,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)

data class UserInfo(
    val _id: String,
    val username: String
)