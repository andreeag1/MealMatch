package com.mealmatch.data.model
import android.media.session.MediaSession.Token

data class UserPreferenceMessage(
    val cuisine: String,
    val dietary: String,
    val ambiance: String,
    val budget: String

)

data class UserProfileMessage(
    val userID: String,
    val username: String,
    val email: String,
    val userPreferenceMessage: UserPreferenceMessage,
)
