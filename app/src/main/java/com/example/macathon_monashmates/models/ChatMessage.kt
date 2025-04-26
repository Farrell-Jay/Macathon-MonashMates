package com.example.macathon_monashmates.models

import java.io.Serializable
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    // Required empty constructor for Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        senderId = "",
        receiverId = "",
        message = "",
        timestamp = System.currentTimeMillis()
    )
} 