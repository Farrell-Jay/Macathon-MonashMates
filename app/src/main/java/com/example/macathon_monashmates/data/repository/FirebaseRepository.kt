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

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val usersCollection = firestore.collection("users")
    private val mentorsCollection = firestore.collection("mentors")
    private val studentsCollection = firestore.collection("students")
    private val chatMessagesCollection = firestore.collection("chat_messages")

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
    suspend fun sendMessage(message: ChatMessage) {
        chatMessagesCollection.document(message.id).set(message).await()
    }

    fun getChatMessages(senderId: String, receiverId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = chatMessagesCollection
            .whereIn("senderId", listOf(senderId, receiverId))
            .whereIn("receiverId", listOf(senderId, receiverId))
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    fun getRecentChats(userId: String): Flow<List<Pair<User, ChatMessage>>> = callbackFlow {
        val recentChatsFlow = MutableStateFlow<List<Pair<User, ChatMessage>>>(emptyList())
        
        val listener = chatMessagesCollection
            .whereEqualTo("senderId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val recentChats = mutableListOf<Pair<User, ChatMessage>>()
                        for (message in messages) {
                            val otherUserId = if (message.senderId == userId) message.receiverId else message.senderId
                            val otherUser = getUser(otherUserId)
                            if (otherUser != null) {
                                recentChats.add(Pair(otherUser, message))
                            }
                        }
                        recentChatsFlow.value = recentChats
                    }
                }
            }
        
        recentChatsFlow.collect { chats ->
            trySend(chats)
        }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
} 