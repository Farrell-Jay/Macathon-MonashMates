package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macathon_monashmates.R
import com.example.macathon_monashmates.managers.UserManager
import com.example.macathon_monashmates.models.User

class ChatHistoryPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatHistoryScreen()
        }
    }
}

@Composable
fun ChatHistoryScreen() {
    val context = LocalContext.current
    val userManager = remember { UserManager(context) }
    val currentUser = remember { userManager.getCurrentUser() }
    
    // Sample chat history data - in a real app, this would come from a database
    val chatHistory = remember {
        listOf(
            ChatHistoryItem(
                user = "John Smith",
                lastMessage = "Hey, are you still interested in the study group?",
                timestamp = "10:30 AM"
            ),
            ChatHistoryItem(
                user = "Sarah Johnson",
                lastMessage = "Thanks for the notes! They were really helpful.",
                timestamp = "Yesterday"
            ),
            ChatHistoryItem(
                user = "Michael Brown",
                lastMessage = "Let's meet at the library tomorrow",
                timestamp = "2 days ago"
            )
        )
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
                onClick = { (context as ComponentActivity).finish() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Chat History",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        
        // Chat List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatHistory) { chatItem ->
                ChatHistoryItem(
                    chatItem = chatItem,
                    onItemClick = {
                        val intent = Intent(context, ChatPage::class.java).apply {
                            putExtra("user", chatItem.user)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun ChatHistoryItem(
    chatItem: ChatHistoryItem,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Image(
                painter = painterResource(id = R.drawable.ic_profile_placeholder),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp)
            )
            
            // Chat Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chatItem.user,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = chatItem.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Timestamp
            Text(
                text = chatItem.timestamp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

data class ChatHistoryItem(
    val user: String,
    val lastMessage: String,
    val timestamp: String
) 