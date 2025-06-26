package com.mealmatch.data.model

data class CreateGroupRequest(
    val name: String,
    val members: List<String>
)

data class MemberResponse(
    val _id: String,
    val username: String,
    val email: String
)

data class GroupResponse(
    val _id: String,
    val name: String,
    val members: List<MemberResponse>,
    val createdAt: String,
    val updatedAt: String
)

data class UserInfo(
    val _id: String,
    val username: String,
)