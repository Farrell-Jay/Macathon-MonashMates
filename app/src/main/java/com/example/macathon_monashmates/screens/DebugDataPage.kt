package com.example.macathon_monashmates.screens

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.macathon_monashmates.managers.ChatHistoryManager
import com.example.macathon_monashmates.managers.UserManager
import com.example.macathon_monashmates.models.User
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.*

class DebugDataPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DebugDataScreen()
        }
    }
}

@Composable
fun DebugDataScreen() {
    val context = LocalContext.current
    val userManager = remember { UserManager(context) }
    val chatHistoryManager = remember { ChatHistoryManager(context) }
    val gson = remember { GsonBuilder().setPrettyPrinting().create() }
    
    val currentUser = userManager.getCurrentUser()
    val usersList = userManager.getUsersList()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Debug Data",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Current User
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current User",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = if (currentUser != null) {
                        gson.toJson(currentUser)
                    } else {
                        "No current user"
                    }
                )
            }
        }
        
        // Users List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "All Users",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(usersList) { user ->
                        Text(
                            text = gson.toJson(user),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
        
        // Chat History
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Chat History",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (currentUser != null) {
                    val recentChats = chatHistoryManager.getRecentChats(currentUser.studentId)
                    LazyColumn {
                        items(recentChats) { (user, message) ->
                            Text(
                                text = "Chat with ${user.name}:\n${gson.toJson(message)}",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                } else {
                    Text("No current user to show chat history")
                }
            }
        }
        
        // SharedPreferences Files
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "SharedPreferences Files",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("Users file: /data/data/com.example.macathon_monashmates/shared_prefs/users.xml")
                Text("Chat history file: /data/data/com.example.macathon_monashmates/shared_prefs/chat_history.xml")
            }
        }
    }
} 