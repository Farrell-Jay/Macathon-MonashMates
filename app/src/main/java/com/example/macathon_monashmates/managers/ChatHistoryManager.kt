package com.example.macathon_monashmates.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.macathon_monashmates.models.ChatMessage
import com.example.macathon_monashmates.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ChatHistoryManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)
    private val userManager = UserManager(context)
    private val gson = Gson()
    private val TAG = "ChatHistoryManager"
    private val UNREAD_COUNT_KEY = "unread_count_"

    fun saveMessage(senderId: String, receiverId: String, message: ChatMessage) {
        try {
            // Save message for sender
            val senderChatHistory = getChatHistory(senderId, receiverId).toMutableList()
            senderChatHistory.add(message)
            val senderJson = gson.toJson(senderChatHistory)
            sharedPreferences.edit().putString("${senderId}_${receiverId}", senderJson).apply()

            // Save message for receiver
            val receiverChatHistory = getChatHistory(receiverId, senderId).toMutableList()
            receiverChatHistory.add(message)
            val receiverJson = gson.toJson(receiverChatHistory)
            sharedPreferences.edit().putString("${receiverId}_${senderId}", receiverJson).apply()

            // Increment unread count for receiver
            incrementUnreadCount(receiverId, senderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving message: ${e.message}")
        }
    }

    fun getChatHistory(userId: String, otherUserId: String): List<ChatMessage> {
        return try {
            val json = sharedPreferences.getString("${userId}_${otherUserId}", null)
            if (json != null) {
                val type = object : TypeToken<List<ChatMessage>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat history: ${e.message}")
            emptyList()
        }
    }

    fun getRecentChats(userId: String): List<Pair<User, ChatMessage>> {
        val allChats = mutableListOf<Pair<User, ChatMessage>>()
        
        try {
            val currentUser = userManager.getCurrentUser() ?: return emptyList()
            val allEntries = sharedPreferences.all
            
            allEntries.forEach { (key, _) ->
                if (key.startsWith("${userId}_")) {
                    val otherUserId = key.substringAfter("${userId}_")
                    val chatHistory = getChatHistory(userId, otherUserId)
                    if (chatHistory.isNotEmpty()) {
                        val lastMessage = chatHistory.last()
                        val otherUser = userManager.getUserById(otherUserId)
                        if (otherUser != null) {
                            allChats.add(Pair(otherUser, lastMessage))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent chats: ${e.message}")
        }
        
        return allChats.sortedByDescending { it.second.timestamp }
    }

    fun getUnreadCount(userId: String, otherUserId: String): Int {
        return try {
            sharedPreferences.getInt("${UNREAD_COUNT_KEY}${userId}_${otherUserId}", 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count: ${e.message}")
            0
        }
    }

    fun clearUnreadCount(userId: String, otherUserId: String) {
        try {
            sharedPreferences.edit().putInt("${UNREAD_COUNT_KEY}${userId}_${otherUserId}", 0).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing unread count: ${e.message}")
        }
    }

    private fun incrementUnreadCount(userId: String, otherUserId: String) {
        try {
            val currentCount = getUnreadCount(userId, otherUserId)
            sharedPreferences.edit().putInt("${UNREAD_COUNT_KEY}${userId}_${otherUserId}", currentCount + 1).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing unread count: ${e.message}")
        }
    }

    fun clearChatHistory(userId: String, otherUserId: String) {
        try {
            sharedPreferences.edit().remove("${userId}_${otherUserId}").apply()
            sharedPreferences.edit().remove("${otherUserId}_${userId}").apply()
            sharedPreferences.edit().remove("${UNREAD_COUNT_KEY}${userId}_${otherUserId}").apply()
            sharedPreferences.edit().remove("${UNREAD_COUNT_KEY}${otherUserId}_${userId}").apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing chat history: ${e.message}")
        }
    }
} 