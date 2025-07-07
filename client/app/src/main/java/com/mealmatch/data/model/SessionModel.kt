package com.mealmatch.data.model

data class MatchSessionResponse(
    val _id: String,
    val group: String?,
    val status: String,
    val restaurants: List<String>,
    val createdAt: String,
    val updatedAt: String
)

data class Swipe(
    val restaurantId: String,
    val liked: Boolean
)

data class SubmitSwipesRequest(
    val swipes: List<Swipe>
)

data class MatchResultResponse(
    val restaurantId: String?,
    val restaurantName: String?
)

data class SessionStatusResponse(
    val status: String
)