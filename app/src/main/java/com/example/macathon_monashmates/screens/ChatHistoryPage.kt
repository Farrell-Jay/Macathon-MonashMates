package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.macathon_monashmates.data.repository.FirebaseRepository
import com.example.macathon_monashmates.managers.UserManager
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
    
    // Get user from UserManager instead of Firebase Auth
    val userManager = remember { UserManager(context) }
    val currentUser = userManager.getCurrentUser()
    
    // Loading and error states
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            if (currentUser != null) {
                Log.d("ChatHistory", "Loading chats for user: ${currentUser.studentId}")
                scope.launch {
                    repository.getRecentChats(currentUser.studentId).collectLatest { chats ->
                        recentChats.clear()
                        recentChats.addAll(chats)
                        isLoading = false
                        Log.d("ChatHistory", "Loaded ${chats.size} chats")
                    }
                }
            } else {
                errorMessage = "User not logged in"
                isLoading = false
            }
        } catch (e: Exception) {
            Log.e("ChatHistory", "Error loading chats", e)
            errorMessage = "Failed to load chats: ${e.message}"
            isLoading = false
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                                if (currentUser != null) {
                                    scope.launch {
                                        try {
                                            repository.getRecentChats(currentUser.studentId).collectLatest { chats ->
                                                recentChats.clear()
                                                recentChats.addAll(chats)
                                                isLoading = false
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Failed to reload: ${e.message}"
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    errorMessage = "User not logged in"
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                recentChats.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No chat history yet",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        Button(
                            onClick = onNavigateToDiscover,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("Discover people to chat with")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
            .clickable(onClick = onItemClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MonashBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MonashBlue
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (user.isMentor) "👨‍🏫" else "👨‍🎓",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    
                    Text(
                        text = lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
} 