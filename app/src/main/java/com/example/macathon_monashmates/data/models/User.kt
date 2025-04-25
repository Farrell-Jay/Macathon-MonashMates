package com.example.macathon_monashmates.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val studentId: String = "",
    val isMentor: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) 