package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
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
import com.example.macathon_monashmates.managers.ChatHistoryManager
import com.example.macathon_monashmates.managers.UserManager
import com.example.macathon_monashmates.models.ChatMessage
import com.example.macathon_monashmates.models.User
import java.text.SimpleDateFormat
import java.util.*

class ChatPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val user = intent.getSerializableExtra("user") as? User
                val source = intent.getStringExtra("source")
                
                if (user != null) {
                    ChatScreen(
                        user = user,
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
}

@Composable
fun ChatScreen(
    user: User,
    onBackClick: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    val chatHistoryManager = remember { ChatHistoryManager(context) }
    val userManager = remember { UserManager(context) }
    val currentUser = userManager.getCurrentUser()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    
    // Load existing messages and clear unread count
    LaunchedEffect(user.studentId) {
        if (currentUser != null) {
            messages.clear()
            messages.addAll(chatHistoryManager.getChatHistory(currentUser.studentId, user.studentId))
            chatHistoryManager.clearUnreadCount(currentUser.studentId, user.studentId)
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
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
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
                .fillMaxWidth(),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(message, currentUser?.studentId == message.senderId)
            }
        }
        
        // Message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
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
                            senderId = currentUser.studentId,
                            receiverId = user.studentId,
                            message = message
                        )
                        messages.add(newMessage)
                        chatHistoryManager.saveMessage(currentUser.studentId, user.studentId, newMessage)
                        message = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary)
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
                containerColor = if (isSentByMe) MaterialTheme.colorScheme.primary 
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

