package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.macathon_monashmates.R
import com.example.macathon_monashmates.models.User

class ChatHistoryPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                ChatHistoryScreen(navController = navController)
            }
        }
    }
}

@Composable
fun ChatHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    
    // Hardcoded chat history data with User objects
    val chatHistory = listOf(
        ChatHistoryItem(
            user = User(
                studentId = "1",
                name = "Dr. Sarah Johnson",
                email = "sarah.johnson@monash.edu",
                isMentor = true,
                subjects = listOf("Mathematics", "Statistics")
            ),
            lastMessage = "I've reviewed your assignment. Let's discuss the improvements needed.",
            timestamp = "10:30 AM"
        ),
        ChatHistoryItem(
            user = User(
                studentId = "2",
                name = "Prof. Michael Brown",
                email = "michael.brown@monash.edu",
                isMentor = true,
                subjects = listOf("Computer Science", "Algorithms")
            ),
            lastMessage = "Great progress on the project! Let's schedule our next meeting.",
            timestamp = "Yesterday"
        ),
        ChatHistoryItem(
            user = User(
                studentId = "3",
                name = "Dr. Emily Chen",
                email = "emily.chen@monash.edu",
                isMentor = true,
                subjects = listOf("Physics", "Quantum Mechanics")
            ),
            lastMessage = "The lab results look promising. We should analyze them together.",
            timestamp = "2 days ago"
        ),
        ChatHistoryItem(
            user = User(
                studentId = "4",
                name = "Prof. David Wilson",
                email = "david.wilson@monash.edu",
                isMentor = true,
                subjects = listOf("Chemistry", "Organic Chemistry")
            ),
            lastMessage = "Your research proposal needs some refinement. Let's work on it.",
            timestamp = "3 days ago"
        )
    )
    
    Scaffold(
        bottomBar = { BottomNavigationBar(currentPage = 2) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatHistory) { chatItem ->
                    ChatHistoryItem(
                        chatItem = chatItem,
                        onItemClick = {
                            val intent = Intent(context, ChatPage::class.java).apply {
                                putExtra("user", chatItem.user)
                                putExtra("source", "chat_history")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = chatItem.user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = if (chatItem.user.isMentor) "Mentor" else "Student",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                // Timestamp
                Text(
                    text = chatItem.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Last Message
            Text(
                text = chatItem.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subjects
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chatItem.user.subjects.forEach { subject ->
                    Text(
                        text = subject,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF009AC7),
                        modifier = Modifier
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

data class ChatHistoryItem(
    val user: User,
    val lastMessage: String,
    val timestamp: String
) 