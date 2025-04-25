package com.example.macathon_monashmates.data.models

data class StudentProfile(
    val uid: String = "",
    val bio: String = "",
    val areasOfInterest: List<String> = listOf(),
    val currentUnits: List<String> = listOf(),
    val academicGoals: String = ""
) 