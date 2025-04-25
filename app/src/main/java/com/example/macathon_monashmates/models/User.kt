package com.example.macathon_monashmates.models

import java.io.Serializable

data class User(
    val name: String,
    val studentId: String,
    val email: String,
    val isMentor: Boolean,
    val subjects: List<String>
) : Serializable 