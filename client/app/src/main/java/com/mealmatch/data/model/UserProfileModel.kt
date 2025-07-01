package com.mealmatch.data.model

data class UserPreferenceMessage(
    val cuisine: String,
    val dietary: String,
    val ambiance: String,
    val budget: String

)

data class UserProfileMessage(
    val userId: String,
    val username: String,
    val email: String,
    val userPreferenceMessage: UserPreferenceMessage,
)
