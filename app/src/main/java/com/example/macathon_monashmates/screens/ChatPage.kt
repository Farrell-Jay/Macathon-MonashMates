package com.example.macathon_monashmates.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ChatPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = ColorScheme(
                    primary = Color(0xFF002A5C),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFF002A5C),
                    onPrimaryContainer = Color.White,
                    inversePrimary = Color(0xFF009AC7),
                    secondary = Color(0xFF009AC7),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFF009AC7),
                    onSecondaryContainer = Color.White,
                    tertiary = Color(0xFF002A5C),
                    onTertiary = Color.White,
                    tertiaryContainer = Color(0xFF002A5C),
                    onTertiaryContainer = Color.White,
                    background = Color(0xFFF4F4F4),
                    onBackground = Color.Black,
                    surface = Color.White,
                    onSurface = Color.Black,
                    surfaceVariant = Color.White,
                    onSurfaceVariant = Color.Black,
                    surfaceTint = Color(0xFF002A5C),
                    inverseSurface = Color(0xFF002A5C),
                    inverseOnSurface = Color.White,
                    error = Color.Red,
                    onError = Color.White,
                    errorContainer = Color.Red,
                    onErrorContainer = Color.White,
                    outline = Color.Gray,
                    outlineVariant = Color.LightGray,
                    scrim = Color.Black.copy(alpha = 0.5f)
                )
            ) {
                ChatScreen()
            }
        }
    }
}

@Composable
fun ChatScreen() {
    Scaffold(
        bottomBar = { BottomNavigationBar(currentPage = 2) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Chat Page",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF002A5C)
            )
            // Add more chat page content here
        }
    }
}

