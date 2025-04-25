package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macathon_monashmates.R
import com.google.ai.client.generativeai.Chat

class DiscoverPage : ComponentActivity() {
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
                DiscoverScreen()
            }
        }
    }
}

@Composable
fun DiscoverScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showMentors by remember { mutableStateOf(true) } // true for mentors, false for students
    
    // Sample data for demonstration
    val mentors = listOf(
        Mentor("Dr. Jane Smith", "FIT2004, FIT3171", "AI Researcher | Data Structures Specialist"),
        Mentor("Prof. John Doe", "FIT2099, FIT3155", "Software Engineering Expert"),
        Mentor("Dr. Sarah Johnson", "FIT2102, FIT3171", "Programming Paradigms Specialist"),
        Mentor("Dr. Michael Brown", "FIT2004, FIT3155", "Algorithms and Data Structures Expert"),
        Mentor("Prof. Emily Davis", "FIT2099, FIT2102", "Object-Oriented Design Specialist")
    )
    
    val students = listOf(
        Student("Alex Johnson", "FIT2004", "Interested in AI and Machine Learning"),
        Student("Sarah Chen", "FIT2099", "Software Development Enthusiast"),
        Student("Michael Brown", "FIT2102", "Web Development Focus"),
        Student("Emily Wilson", "FIT3171", "Data Science and Analytics"),
        Student("David Lee", "FIT3155", "Full Stack Development")
    )
    
    Scaffold(
        bottomBar = { BottomNavigationBar(currentPage = 1) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F4F4))
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Discover",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF002A5C)
                )
                Text(
                    text = "Browse by subject or expertise",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                // User Type Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = showMentors,
                        onClick = { showMentors = true },
                        label = { Text("Mentors") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Mentors"
                            )
                        }
                    )
                    FilterChip(
                        selected = !showMentors,
                        onClick = { showMentors = false },
                        label = { Text("Students") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Students"
                            )
                        }
                    )
                }
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (showMentors) "Search mentors..." else "Search students...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF009AC7)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF009AC7),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
            
            // User List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showMentors) {
                    items(mentors) { mentor ->
                        MentorCard(mentor)
                    }
                } else {
                    items(students) { student ->
                        StudentCard(student)
                    }
                }
            }
        }
    }
}

@Composable
fun MentorCard(mentor: Mentor) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF009AC7))
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }
            
            // Mentor Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = mentor.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF002A5C)
                )
                Text(
                    text = mentor.subjects,
                    fontSize = 14.sp,
                    color = Color(0xFF009AC7),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = mentor.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // View Profile Button
            IconButton(
                onClick = { /* TODO: Handle view profile */ },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Profile",
                    tint = Color(0xFF009AC7)
                )
            }
        }
    }
}

@Composable
fun StudentCard(student: Student) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF009AC7))
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Student",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }
            
            // Student Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = student.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF002A5C)
                )
                Text(
                    text = student.subject,
                    fontSize = 14.sp,
                    color = Color(0xFF009AC7),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = student.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // View Profile Button
            IconButton(
                onClick = { /* TODO: Handle view profile */ },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Profile",
                    tint = Color(0xFF009AC7)
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentPage: Int = 1) {
    val context = LocalContext.current
    
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.height(64.dp)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            selected = currentPage == 0,
            onClick = { 
                val intent = Intent(context, HomePage::class.java)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF009AC7),
                selectedTextColor = Color(0xFF009AC7),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Discover"
                )
            },
            label = { Text("Discover") },
            selected = currentPage == 1,
            onClick = { 
                val intent = Intent(context, DiscoverPage::class.java)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF009AC7),
                selectedTextColor = Color(0xFF009AC7),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat"
                )
            },
            label = { Text("Chat") },
            selected = currentPage == 2,
            onClick = { 
                val intent = Intent(context, ChatPage::class.java)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF009AC7),
                selectedTextColor = Color(0xFF009AC7),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            selected = currentPage == 3,
            onClick = { 
                val intent = Intent(context, ProfilePage::class.java)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF009AC7),
                selectedTextColor = Color(0xFF009AC7),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

data class Mentor(
    val name: String,
    val subjects: String,
    val description: String
)

data class Student(
    val name: String,
    val subject: String,
    val description: String
)

