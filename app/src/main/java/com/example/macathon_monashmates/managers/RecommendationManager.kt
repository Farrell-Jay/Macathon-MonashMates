package com.example.macathon_monashmates.managers

import android.content.Context
import android.util.Log
import com.example.macathon_monashmates.data.models.MentorProfile
import com.example.macathon_monashmates.data.models.StudentProfile
import com.example.macathon_monashmates.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class RecommendationManager(private val context: Context) {
    
    private val TAG = "RecommendationManager"
    private val db = FirebaseFirestore.getInstance()
    
    companion object {
        private var INSTANCE: RecommendationManager? = null
        
        fun getInstance(context: Context): RecommendationManager {
            return INSTANCE ?: synchronized(this) {
                val instance = RecommendationManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
    
    init {
        Log.d(TAG, "Recommendation Manager initialized")
    }
    
    // For demonstration purposes, using a rule-based logic for recommendations
    suspend fun getRecommendedMentors(currentUser: User, limit: Int = 5): List<Pair<MentorProfile, User>> = withContext(Dispatchers.IO) {
        try {
            // Get all mentors
            val mentorDocs = db.collection("mentors").get().await()
            val allMentors = mentorDocs.mapNotNull { doc ->
                try {
                    val uid = doc.getString("uid") ?: return@mapNotNull null
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val email = doc.getString("email") ?: return@mapNotNull null
                    
                    val user = User(
                        name = name,
                        studentId = uid,
                        email = email,
                        isMentor = true,
                        subjects = doc.get("unitsTaken") as? List<String> ?: emptyList()
                    )
                    
                    MentorProfile(
                        uid = uid,
                        bio = doc.getString("bio") ?: "",
                        areasOfExpertise = (doc.get("expertise") as? List<String>) ?: emptyList(),
                        unitsTaken = (doc.get("unitsTaken") as? List<String>) ?: emptyList(),
                        availability = emptyList() // Simplified for the example
                    ) to user
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing mentor: ${e.message}")
                    null
                }
            }
            
            // Calculate relevance scores using rule-based logic
            val scoredMentors = allMentors.map { mentorPair ->
                val (mentor, user) = mentorPair
                
                // Calculate a relevance score based on shared subjects
                val sharedSubjects = mentor.unitsTaken.intersect(currentUser.subjects.toSet())
                val subjectScore = sharedSubjects.size.toFloat() * 10f

                // Additional score for expertise areas
                val expertiseScore = if (mentor.areasOfExpertise.isNotEmpty()) 5f else 0f
                
                // Combined score (higher is better)
                val totalScore = subjectScore + expertiseScore
                
                Triple(mentor, user, totalScore)
            }
            
            // Sort by score (highest first) and take top 'limit' results
            scoredMentors
                .sortedByDescending { it.third }
                .take(limit)
                .map { Pair(it.first, it.second) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommended mentors: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getRecommendedStudents(currentUser: User, limit: Int = 5): List<Pair<StudentProfile, User>> = withContext(Dispatchers.IO) {
        try {
            // Get all students
            val studentDocs = db.collection("students").get().await()
            val allStudents = studentDocs.mapNotNull { doc ->
                try {
                    val uid = doc.getString("uid") ?: return@mapNotNull null
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val email = doc.getString("email") ?: return@mapNotNull null
                    
                    // Skip if this is the current user
                    if (uid == currentUser.studentId) return@mapNotNull null
                    
                    val user = User(
                        name = name,
                        studentId = uid,
                        email = email,
                        isMentor = false,
                        subjects = doc.get("units") as? List<String> ?: emptyList()
                    )
                    
                    StudentProfile(
                        uid = uid,
                        bio = doc.getString("bio") ?: "",
                        areasOfInterest = (doc.get("majors") as? List<String>) ?: emptyList(),
                        currentUnits = (doc.get("units") as? List<String>) ?: emptyList(),
                        academicGoals = doc.getString("academicGoals") ?: ""
                    ) to user
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing student: ${e.message}")
                    null
                }
            }
            
            // Calculate relevance scores using rule-based logic
            val scoredStudents = allStudents.map { studentPair ->
                val (student, user) = studentPair
                
                // For mentors looking for students, match based on mentor's expertise and student's interests
                val relevantUnits = if (currentUser.isMentor) {
                    student.currentUnits.intersect(currentUser.subjects.toSet())
                } else {
                    student.currentUnits.intersect(currentUser.subjects.toSet())
                }
                
                val unitScore = relevantUnits.size.toFloat() * 10f
                
                // Additional score for having academic goals
                val goalScore = if (student.academicGoals.isNotEmpty()) 5f else 0f
                
                // Combined score (higher is better)
                val totalScore = unitScore + goalScore
                
                Triple(student, user, totalScore)
            }
            
            // Sort by score (highest first) and take top 'limit' results
            scoredStudents
                .sortedByDescending { it.third }
                .take(limit)
                .map { Pair(it.first, it.second) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommended students: ${e.message}")
            emptyList()
        }
    }
    
    // Generate personalized recommendation reasons
    fun generateMentorReason(currentUser: User, mentorProfile: MentorProfile): String {
        val sharedSubjects = mentorProfile.unitsTaken.intersect(currentUser.subjects.toSet())
        
        return when {
            sharedSubjects.isNotEmpty() -> {
                val subjectsList = sharedSubjects.take(2).joinToString(", ")
                val extraSubjects = if (sharedSubjects.size > 2) " and ${sharedSubjects.size - 2} more" else ""
                "Shares ${sharedSubjects.size} subjects with you: $subjectsList$extraSubjects"
            }
            mentorProfile.areasOfExpertise.isNotEmpty() -> {
                val expertiseList = mentorProfile.areasOfExpertise.take(2).joinToString(", ")
                val relevantUnits = currentUser.subjects.take(2).joinToString(", ")
                if (currentUser.subjects.isNotEmpty()) {
                    "Expert in $expertiseList, relevant for $relevantUnits"
                } else {
                    "Expert in $expertiseList"
                }
            }
            else -> "Recommended based on your academic interests"
        }
    }
    
    fun generateStudentReason(currentUser: User, studentProfile: StudentProfile): String {
        val sharedSubjects = studentProfile.currentUnits.intersect(currentUser.subjects.toSet())
        
        return when {
            sharedSubjects.isNotEmpty() -> {
                val subjectsList = sharedSubjects.take(2).joinToString(", ")
                val extraSubjects = if (sharedSubjects.size > 2) " and ${sharedSubjects.size - 2} more" else ""
                "Taking ${sharedSubjects.size} units that match your expertise: $subjectsList$extraSubjects"
            }
            studentProfile.academicGoals.isNotEmpty() -> {
                val shortGoal = studentProfile.academicGoals.take(50) + 
                    if (studentProfile.academicGoals.length > 50) "..." else ""
                "Has academic goals: \"$shortGoal\""
            }
            studentProfile.areasOfInterest.isNotEmpty() -> {
                val interestList = studentProfile.areasOfInterest.take(2).joinToString(", ")
                val relevantUnits = currentUser.subjects.take(2).joinToString(", ")
                if (currentUser.subjects.isNotEmpty()) {
                    "Interested in $interestList, relevant for $relevantUnits"
                } else {
                    "Interested in $interestList"
                }
            }
            else -> "Looking for guidance in your areas of expertise"
        }
    }

    // Add a new method for getting more detailed unit comparison
    fun getUnitComparisonDetails(user1Units: List<String>, user2Units: List<String>): UnitComparisonDetails {
        val sharedUnits = user1Units.intersect(user2Units.toSet())
        val uniqueToUser1 = user1Units.filterNot { sharedUnits.contains(it) }
        val uniqueToUser2 = user2Units.filterNot { sharedUnits.contains(it) }
        
        return UnitComparisonDetails(
            sharedUnits = sharedUnits.toList(),
            uniqueToUser1 = uniqueToUser1,
            uniqueToUser2 = uniqueToUser2,
            compatibilityScore = calculateCompatibilityScore(sharedUnits.size, user1Units.size, user2Units.size)
        )
    }

    private fun calculateCompatibilityScore(sharedCount: Int, user1Count: Int, user2Count: Int): Float {
        // Jaccard similarity coefficient: intersection size / union size
        val unionSize = user1Count + user2Count - sharedCount
        return if (unionSize > 0) {
            (sharedCount.toFloat() / unionSize) * 100f
        } else {
            0f
        }
    }

    // Add a data class to hold the unit comparison details
    data class UnitComparisonDetails(
        val sharedUnits: List<String>,
        val uniqueToUser1: List<String>,
        val uniqueToUser2: List<String>,
        val compatibilityScore: Float
    )
} 