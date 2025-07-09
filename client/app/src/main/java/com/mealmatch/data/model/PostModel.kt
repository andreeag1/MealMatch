package com.mealmatch.data.model

data class Post(
    val _id: String?,
    val caption: String,
    val rating: Float = 0f,
    val imageUrl: String? = null,
    val user: PostUser
)

data class PostUser(
    val username: String
)