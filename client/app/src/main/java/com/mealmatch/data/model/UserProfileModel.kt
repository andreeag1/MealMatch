package com.mealmatch.data.model
import android.media.session.MediaSession.Token

data class UserPreferences(
    val cuisine: String,
    val dietary: String,
    val ambiance: String,
    val budget: String

)
