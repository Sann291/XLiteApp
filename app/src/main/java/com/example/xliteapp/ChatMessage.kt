package com.example.xliteapp

data class ChatMessage(
    val senderId: String? = null,
    val receiverId: String? = null,
    val message: String? = null,
    val timestamp: Long = 0
)