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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.macathon_monashmates.data.repository.FirebaseRepository
import com.example.macathon_monashmates.models.ChatMessage
import com.example.macathon_monashmates.models.User
import com.example.macathon_monashmates.utils.MonashBlue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatHistoryPage : ComponentActivity() {
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ChatHistoryScreen(
                    repository = repository,
                    onNavigateToChat = { user ->
                        val intent = Intent(this@ChatHistoryPage, ChatPage::class.java).apply {
                            putExtra("user", user)
                            putExtra("source", "chat_history")
                        }
                        startActivity(intent)
                    },
                    onNavigateToDiscover = {
                        val intent = Intent(this@ChatHistoryPage, DiscoverPage::class.java)
                        startActivity(intent)
                    },
                    onNavigateToHome = {
                        val intent = Intent(this@ChatHistoryPage, HomePage::class.java)
                        startActivity(intent)
                    },
                    onNavigateToProfile = {
                        val intent = Intent(this@ChatHistoryPage, ProfilePage::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(
    repository: FirebaseRepository,
    onNavigateToChat: (User) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val recentChats = remember { mutableStateListOf<Pair<User, ChatMessage>>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUser = repository.getCurrentUser()
    
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            scope.launch {
                repository.getRecentChats(currentUser.uid).collectLatest { chats ->
                    recentChats.clear()
                    recentChats.addAll(chats)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MonashBlue
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = MonashBlue
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onNavigateToHome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Discover") },
                    label = { Text("Discover") },
                    selected = false,
                    onClick = onNavigateToDiscover
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(recentChats) { (user, message) ->
                ChatHistoryItem(
                    user = user,
                    lastMessage = message.message,
                    timestamp = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(message.timestamp)),
                    onItemClick = { onNavigateToChat(user) }
                )
            }
        }
    }
}

@Composable
fun ChatHistoryItem(
    user: User,
    lastMessage: String,
    timestamp: String,
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
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = if (user.isMentor) "Mentor" else "Student",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                // Timestamp
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Last Message
            Text(
                text = lastMessage,
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
                user.subjects.forEach { subject ->
                    Text(
                        text = subject,
                        style = MaterialTheme.typography.bodySmall,
                        color = MonashBlue,
                        modifier = Modifier
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
} 