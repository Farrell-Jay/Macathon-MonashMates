package com.example.macathon_monashmates.data.repository

import com.example.macathon_monashmates.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val usersCollection = firestore.collection("users")
    private val mentorsCollection = firestore.collection("mentors")
    private val studentsCollection = firestore.collection("students")

    // User operations
    suspend fun createUser(user: User) {
        usersCollection.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        return try {
            usersCollection.document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Mentor operations
    suspend fun createOrUpdateMentorProfile(profile: MentorProfile) {
        mentorsCollection.document(profile.uid).set(profile).await()
    }

    suspend fun getMentorProfile(uid: String): MentorProfile? {
        return try {
            mentorsCollection.document(uid).get().await().toObject(MentorProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getAllMentors(): Flow<List<MentorProfile>> = flow {
        try {
            val snapshot = mentorsCollection.get().await()
            val mentors = snapshot.toObjects(MentorProfile::class.java)
            emit(mentors)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Student operations
    suspend fun createOrUpdateStudentProfile(profile: StudentProfile) {
        studentsCollection.document(profile.uid).set(profile).await()
    }

    suspend fun getStudentProfile(uid: String): StudentProfile? {
        return try {
            studentsCollection.document(uid).get().await().toObject(StudentProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getAllStudents(): Flow<List<StudentProfile>> = flow {
        try {
            val snapshot = studentsCollection.get().await()
            val students = snapshot.toObjects(StudentProfile::class.java)
            emit(students)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Authentication operations
    fun getCurrentUser() = auth.currentUser

    suspend fun signOut() {
        auth.signOut()
    }
} 