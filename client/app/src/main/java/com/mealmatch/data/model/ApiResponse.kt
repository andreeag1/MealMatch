package com.mealmatch.data.model

// A generic class to handle all API responses
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val token: String? = null
)