package com.example.macathon_monashmates.data.repository

import com.example.macathon_monashmates.data.models.*
import com.example.macathon_monashmates.models.ChatMessage
import com.example.macathon_monashmates.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.GenericTypeIndicator
import android.util.Log

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val usersCollection = firestore.collection("users")
    private val mentorsCollection = firestore.collection("mentors")
    private val studentsCollection = firestore.collection("students")
    
    // Realtime Database references
    private val chatsRef = database.getReference("chats")
    private val chatMetadataRef = database.getReference("chat_metadata")

    // User operations
    suspend fun createUser(user: User) {
        usersCollection.document(user.studentId).set(user).await()
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
    }.flowOn(Dispatchers.IO)

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
    }.flowOn(Dispatchers.IO)

    // Authentication operations
    fun getCurrentUser() = auth.currentUser

    suspend fun signOut() {
        auth.signOut()
    }

    // Chat operations
    private fun getChatId(user1Id: String, user2Id: String): String {
        // Create a consistent chat ID by sorting the user IDs alphabetically
        return if (user1Id < user2Id) {
            "${user1Id}_${user2Id}"
        } else {
            "${user2Id}_${user1Id}"
        }
    }

    suspend fun sendMessage(message: ChatMessage) {
        try {
            val chatId = getChatId(message.senderId, message.receiverId)
            Log.d("FirebaseRepository", "Sending message to chat: $chatId")
            
            // Save the message to the chat
            val messageRef = chatsRef.child(chatId).child(message.id)
            messageRef.setValue(message).await()
            
            // Update chat metadata
            val metadata = mapOf(
                "lastMessage" to message.message,
                "lastMessageSender" to message.senderId,
                "lastMessageTime" to message.timestamp,
                "participants" to listOf(message.senderId, message.receiverId)
            )
            chatMetadataRef.child(chatId).updateChildren(metadata).await()
            
            Log.d("FirebaseRepository", "Message sent successfully")
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error sending message", e)
            throw e
        }
    }

    fun getChatMessages(user1Id: String, user2Id: String): Flow<List<ChatMessage>> = callbackFlow {
        try {
            val chatId = getChatId(user1Id, user2Id)
            Log.d("FirebaseRepository", "Getting messages for chat: $chatId")
            
            val chatMessagesRef = chatsRef.child(chatId)
            val messagesList = mutableListOf<ChatMessage>()
            
            val valueListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseRepository", "Data changed, snapshot has ${snapshot.childrenCount} messages")
                    messagesList.clear()
                    
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(ChatMessage::class.java)
                        if (message != null) {
                            messagesList.add(message)
                        }
                    }
                    
                    // Sort by timestamp
                    messagesList.sortBy { it.timestamp }
                    trySend(messagesList)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseRepository", "Error getting messages: ${error.message}")
                    trySend(emptyList())
                }
            }
            
            chatMessagesRef.addValueEventListener(valueListener)
            
            awaitClose {
                chatMessagesRef.removeEventListener(valueListener)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error in getChatMessages", e)
            trySend(emptyList())
            close(e)
        }
    }.flowOn(Dispatchers.IO)

    fun getRecentChats(userId: String): Flow<List<Pair<User, ChatMessage>>> = callbackFlow {
        try {
            Log.d("FirebaseRepository", "Getting recent chats for user: $userId")
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseRepository", "Chat metadata changed, snapshot has ${snapshot.childrenCount} chats")
                    
                    scope.launch {
                        val recentChats = mutableListOf<Pair<User, ChatMessage>>()
                        
                        for (chatSnapshot in snapshot.children) {
                            val chatId = chatSnapshot.key ?: continue
                            
                            // Get participants list
                            val participantsSnapshot = chatSnapshot.child("participants")
                            val participants = ArrayList<String>()
                            for (participantSnapshot in participantsSnapshot.children) {
                                val participant = participantSnapshot.getValue(String::class.java)
                                if (participant != null) {
                                    participants.add(participant)
                                }
                            }
                            
                            // Check if user is a participant
                            if (participants.contains(userId)) {
                                // Get the other participant ID
                                val otherUserId = participants.find { it != userId } ?: continue
                                
                                // Get message data
                                val lastMessage = chatSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                                val lastMessageSender = chatSnapshot.child("lastMessageSender").getValue(String::class.java) ?: ""
                                val lastMessageTime = chatSnapshot.child("lastMessageTime").getValue(Long::class.java) ?: 0L
                                
                                // Create a message object
                                val chatMessage = ChatMessage(
                                    id = "meta-$chatId",
                                    senderId = lastMessageSender,
                                    receiverId = if (lastMessageSender == userId) otherUserId else userId,
                                    message = lastMessage,
                                    timestamp = lastMessageTime
                                )
                                
                                // Get the other user's profile
                                val otherUser = getUserByStudentId(otherUserId)
                                if (otherUser != null) {
                                    recentChats.add(Pair(otherUser, chatMessage))
                                }
                            }
                        }
                        
                        // Sort by most recent message
                        recentChats.sortByDescending { it.second.timestamp }
                        trySend(recentChats)
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseRepository", "Error getting recent chats: ${error.message}")
                    trySend(emptyList())
                }
            }
            
            chatMetadataRef.addValueEventListener(listener)
            
            awaitClose {
                chatMetadataRef.removeEventListener(listener)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error in getRecentChats", e)
            trySend(emptyList())
            close(e)
        }
    }.flowOn(Dispatchers.IO)
    
    // Helper function to get user by student ID
    private suspend fun getUserByStudentId(studentId: String): User? {
        // First try from users collection
        val user = getUser(studentId)
        if (user != null) {
            return user
        }
        
        // If not found, try mentors collection
        val mentorSnapshot = mentorsCollection.document(studentId).get().await()
        if (mentorSnapshot.exists()) {
            val name = mentorSnapshot.getString("name") ?: "Unknown Mentor"
            val email = mentorSnapshot.getString("email") ?: ""
            val subjects = mentorSnapshot.get("subjects") as? List<String> ?: listOf()
            
            return User(
                studentId = studentId,
                name = name,
                email = email,
                subjects = subjects,
                isMentor = true
            )
        }
        
        // Lastly, try students collection
        val studentSnapshot = studentsCollection.document(studentId).get().await()
        if (studentSnapshot.exists()) {
            val name = studentSnapshot.getString("name") ?: "Unknown Student"
            val email = studentSnapshot.getString("email") ?: ""
            val subjects = studentSnapshot.get("subjects") as? List<String> ?: listOf()
            
            return User(
                studentId = studentId,
                name = name,
                email = email,
                subjects = subjects,
                isMentor = false
            )
        }
        
        return null
    }
} 