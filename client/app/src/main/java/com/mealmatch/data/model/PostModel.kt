package com.mealmatch.data.model

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("_id")
    val _id: String?,
    val caption: String,
    val rating: Float = 0f,
    val media: List<Media> = emptyList(),
    val user: PostUser,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class PostUser(
    val username: String
)