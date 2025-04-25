package com.example.macathon_monashmates.data.models

data class MentorProfile(
    val uid: String = "",
    val bio: String = "",
    val areasOfExpertise: List<String> = listOf(),
    val unitsTaken: List<String> = listOf(),
    val availability: List<TimeSlot> = listOf()
)

data class TimeSlot(
    val dayOfWeek: Int = 0, // 1 = Monday, 7 = Sunday
    val startTime: String = "", // Format: "HH:mm"
    val endTime: String = ""   // Format: "HH:mm"
) 