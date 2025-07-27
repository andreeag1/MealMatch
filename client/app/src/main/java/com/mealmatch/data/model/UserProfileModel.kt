package com.mealmatch.data.model

data class UserPreferences(
    val cuisine: String,
    val dietary: String,
    val ambiance: String,
    val budget: String
)

data class UserProfileResponse(
    val _id: String,
    val username: String,
    val email: String
)