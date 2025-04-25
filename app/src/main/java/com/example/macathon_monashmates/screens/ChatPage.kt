package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macathon_monashmates.R
import com.example.macathon_monashmates.data.repository.FirebaseRepository
import com.example.macathon_monashmates.managers.ChatHistoryManager
import com.example.macathon_monashmates.managers.UserManager
import com.example.macathon_monashmates.models.ChatMessage
import com.example.macathon_monashmates.models.User
import com.example.macathon_monashmates.utils.MonashBlue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatPage : ComponentActivity() {
    private val repository = FirebaseRepository()
    private lateinit var auth: FirebaseAuth
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        
        // Add auth state listener
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                // User is signed out, redirect to login
                val intent = Intent(this, LoginPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        
        setContent {
            MaterialTheme {
                val user = intent.getSerializableExtra("user") as? User
                val source = intent.getStringExtra("source")
                
                if (user != null) {
                    ChatScreen(
                        user = user,
                        repository = repository,
                        onBackClick = {
                            when (source) {
                                "chat_history" -> {
                                    val intent = Intent(this, ChatHistoryPage::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                "discover" -> {
                                    val intent = Intent(this, DiscoverPage::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                else -> {
                                    val intent = Intent(this, HomePage::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                    )
                } else {
                    // Handle error case
                    Text("Error: No user data provided")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        authStateListener?.let { auth.addAuthStateListener(it) }
    }

    override fun onStop() {
        super.onStop()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}

@Composable
fun ChatScreen(
    user: User,
    repository: FirebaseRepository,
    onBackClick: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    
    // Load messages in real-time
    LaunchedEffect(user.studentId) {
        if (currentUser != null) {
            try {
                Toast.makeText(context, "Starting to load messages...", Toast.LENGTH_SHORT).show()
                Toast.makeText(context, "Current User ID: ${currentUser.uid}", Toast.LENGTH_SHORT).show()
                Toast.makeText(context, "Other User ID: ${user.studentId}", Toast.LENGTH_SHORT).show()
                
                repository.getChatMessages(currentUser.uid, user.studentId).collect { newMessages ->
                    Toast.makeText(context, "Received ${newMessages.size} messages", Toast.LENGTH_SHORT).show()
                    messages.clear()
                    messages.addAll(newMessages.sortedBy { it.timestamp })
                    Toast.makeText(context, "Messages updated: ${messages.size}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error loading messages", e)
                Toast.makeText(context, "Error loading messages: ${e.message}", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "Error at: ${e.stackTrace[0]}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Current user is null! Please sign in again.", Toast.LENGTH_LONG).show()
            // Redirect to login if user is not authenticated
            val intent = Intent(context, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        
        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Toast.makeText(context, "Displaying ${messages.size} messages", Toast.LENGTH_SHORT).show()
            items(messages) { message ->
                MessageBubble(message, message.senderId == currentUser?.uid)
            }
        }
        
        // Message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("Type a message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (message.isNotBlank() && currentUser != null) {
                        val newMessage = ChatMessage(
                            senderId = currentUser.uid,
                            receiverId = user.studentId,
                            message = message,
                            timestamp = System.currentTimeMillis()
                        )
                        scope.launch {
                            try {
                                Toast.makeText(context, "Sending message...", Toast.LENGTH_SHORT).show()
                                repository.sendMessage(newMessage)
                                message = ""
                                Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("ChatScreen", "Error sending message", e)
                                Toast.makeText(context, "Failed to send message: ${e.message}", Toast.LENGTH_LONG).show()
                                Toast.makeText(context, "Error at: ${e.stackTrace[0]}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Cannot send: ${if (message.isBlank()) "message is blank" else "current user is null"}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MonashBlue)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isSentByMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSentByMe) MonashBlue 
                                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.message,
                    color = if (isSentByMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(message.timestamp)),
                    fontSize = 12.sp,
                    color = if (isSentByMe) Color.White.copy(alpha = 0.7f) 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

