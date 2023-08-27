package com.example.chatapp.model

data class Message(
    var messageId: String? = null,
    var message: String? = null,
    var senderId: String? = null,
    var imageUrl: String? = null,
    var timestamp: Long = 0
) {
}