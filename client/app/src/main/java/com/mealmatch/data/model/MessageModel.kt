package com.mealmatch.data.model

data class MessageResponse(
    val _id: String,
    val group: String,
    val user: UserInfo,
    val content: String,
    val createdAt: String
)

// Represents a message being sent via WebSocket
data class WebSocketSendMessage(
    val type: String = "message",
    val roomId: String,
    val content: String
)

// For the 'join' event
data class WebSocketJoinMessage(
    val type: String = "join",
    val roomId: String
)

// Represents a message received from WebSocket
data class WebSocketIncomingMessage(
    val userId: String,
    val username: String,
    val content: String,
    val createdAt: String
)

