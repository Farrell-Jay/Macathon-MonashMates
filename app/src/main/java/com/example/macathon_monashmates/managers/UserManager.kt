package com.example.macathon_monashmates.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.macathon_monashmates.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class UserManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val TAG = "UserManager"
    private val CURRENT_USER_KEY = "current_user"
    private val USERS_LIST_KEY = "users_list"

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
} 