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
import androidx.compose.ui.text.style.TextAlign
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                                "profile_view" -> {
                                    finish() // Just go back to previous screen
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
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Get the current user from UserManager instead of Firebase Auth
    val userManager = remember { UserManager(context) }
    val currentUser = userManager.getCurrentUser()
    
    // Load messages in real-time
    LaunchedEffect(user.studentId) {
        try {
            isLoading = true
            errorMessage = null
            
            if (currentUser != null) {
                // Get the chat ID for logging
                val chatId = if (currentUser.studentId < user.studentId) 
                    "${currentUser.studentId}_${user.studentId}" 
                else 
                    "${user.studentId}_${currentUser.studentId}"
                
                Log.d("ChatScreen", "Setting up message listener for chat ID: $chatId")
                Log.d("ChatScreen", "Current user ID: ${currentUser.studentId}")
                Log.d("ChatScreen", "Other user ID: ${user.studentId}")
                
                // Load messages using the repository
                repository.getChatMessages(currentUser.studentId, user.studentId).collect { newMessages ->
                    Log.d("ChatScreen", "Received ${newMessages.size} messages")
                    messages.clear()
                    messages.addAll(newMessages.sortedBy { it.timestamp })
                    isLoading = false
                }
            } else {
                errorMessage = "Current user information not found. Please log in again."
                isLoading = false
            }
        } catch (e: Exception) {
            Log.e("ChatScreen", "Error loading messages", e)
            errorMessage = "Failed to load messages: ${e.message}"
            isLoading = false
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
        
        // Messages List or Loading/Error state
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        // Attempt to load messages again
                                        if (currentUser != null) {
                                            repository.getChatMessages(currentUser.studentId, user.studentId).collect { newMessages ->
                                                messages.clear()
                                                messages.addAll(newMessages.sortedBy { it.timestamp })
                                                isLoading = false
                                            }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to reload: ${e.message}"
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                messages.isEmpty() -> {
                    Text(
                        text = "No messages yet. Start the conversation!",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { message ->
                            MessageBubble(message, message.senderId == currentUser?.studentId)
                        }
                    }
                }
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
                shape = RoundedCornerShape(24.dp),
                enabled = !isLoading && errorMessage == null
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            var isSending by remember { mutableStateOf(false) }
            
            IconButton(
                onClick = {
                    if (message.isNotBlank() && currentUser != null && !isSending) {
                        isSending = true
                        val chatId = if (currentUser.studentId < user.studentId) 
                            "${currentUser.studentId}_${user.studentId}" 
                        else 
                            "${user.studentId}_${currentUser.studentId}"
                            
                        Log.d("ChatScreen", "Sending message to chat ID: $chatId")
                        
                        val newMessage = ChatMessage(
                            senderId = currentUser.studentId,
                            receiverId = user.studentId,
                            message = message,
                            timestamp = System.currentTimeMillis()
                        )
                        
                        val tempMessage = message
                        message = "" // Clear input field immediately for better UX
                        
                        scope.launch {
                            try {
                                repository.sendMessage(newMessage)
                                Log.d("ChatScreen", "Message sent successfully")
                            } catch (e: Exception) {
                                Log.e("ChatScreen", "Error sending message", e)
                                // Show error and restore message text
                                message = tempMessage
                                Toast.makeText(
                                    context, 
                                    "Failed to send: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isSending = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (message.isNotBlank() && !isSending) MonashBlue else Color.Gray,
                        shape = RoundedCornerShape(24.dp)
                    ),
                enabled = message.isNotBlank() && !isSending && !isLoading && errorMessage == null
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
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

