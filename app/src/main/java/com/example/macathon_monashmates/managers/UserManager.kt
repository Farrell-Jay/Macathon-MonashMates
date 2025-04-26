package com.example.macathon_monashmates.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.macathon_monashmates.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val TAG = "UserManager"
    private val CURRENT_USER_KEY = "current_user"
    private val USERS_LIST_KEY = "users_list"
    private val auth = Firebase.auth

    fun saveUser(user: User) {
        val users = getUsers().toMutableList()
        val existingUserIndex = users.indexOfFirst { it.studentId == user.studentId }
        
        if (existingUserIndex != -1) {
            users[existingUserIndex] = user
        } else {
            users.add(user)
        }
        
        val usersJson = gson.toJson(users)
        sharedPreferences.edit().putString("users", usersJson).apply()
    }
    
    fun getUsers(): List<User> {
        val usersJson = sharedPreferences.getString("users", "[]")
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(usersJson, type) ?: emptyList()
    }
    
    fun getUserByStudentId(studentId: String): User? {
        return getUsers().find { it.studentId == studentId }
    }
    
    fun setCurrentUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString("currentUser", userJson).apply()
        
        // Also sign in with Firebase if not already signed in
        signInWithFirebase(user.studentId)
    }
    
    fun getCurrentUser(): User? {
        val userJson = sharedPreferences.getString("currentUser", null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }
    
    fun clearCurrentUser() {
        sharedPreferences.edit().remove("currentUser").apply()
        // Also sign out from Firebase
        auth.signOut()
    }

    fun getUserById(userId: String): User? {
        return try {
            val userJson = sharedPreferences.getString(userId, null)
            if (userJson != null) {
                gson.fromJson(userJson, User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID: ${e.message}")
            null
        }
    }

    fun getUsersList(): List<User> {
        return try {
            val usersListJson = sharedPreferences.getString(USERS_LIST_KEY, null)
            if (usersListJson != null) {
                val type = object : TypeToken<List<User>>() {}.type
                gson.fromJson(usersListJson, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users list: ${e.message}")
            emptyList()
        }
    }
    
    // New method to authenticate with Firebase
    fun signInWithFirebase(studentId: String) {
        val email = "user_${studentId}@monashmates.com"
        val password = "password123" // Using a default password for demo purposes
        
        // Check if already signed in with correct user
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.uid == studentId) {
            Log.d(TAG, "Already signed in as $studentId")
            return
        }
        
        // Sign out if signed in as someone else
        if (currentUser != null) {
            auth.signOut()
        }
        
        // Try to sign in
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully signed in to Firebase as $studentId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to sign in to Firebase: ${exception.message}")
                
                // If sign in fails, try to create the account
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Log.d(TAG, "Created and signed in to Firebase as $studentId")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to create Firebase account: ${e.message}")
                    }
            }
    }
    
    // Check and sync Firebase auth with current user
    fun syncFirebaseAuth() {
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            signInWithFirebase(currentUser.studentId)
        }
    }
} 