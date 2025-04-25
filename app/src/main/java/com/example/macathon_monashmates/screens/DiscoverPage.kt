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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macathon_monashmates.R
import com.example.macathon_monashmates.models.User
import com.example.macathon_monashmates.data.models.MentorProfile
import com.example.macathon_monashmates.data.models.StudentProfile
import com.example.macathon_monashmates.data.models.TimeSlot
import com.example.macathon_monashmates.data.repository.FirebaseRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DiscoverPage : ComponentActivity() {
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DiscoverScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showMentors by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()
    
    var mentors by remember { mutableStateOf<List<Pair<MentorProfile, User>>>(emptyList()) }
    var students by remember { mutableStateOf<List<Pair<StudentProfile, User>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load data from Firestore
    LaunchedEffect(showMentors) {
        try {
            isLoading = true
            errorMessage = null
            
            if (showMentors) {
                val mentorDocs = db.collection("mentors").get().await()
                mentors = mentorDocs.mapNotNull { doc ->
                    try {
                        val studentId = doc.getString("uid") ?: return@mapNotNull null
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val email = doc.getString("email") ?: return@mapNotNull null
                        
                        val user = User(
                            name = name,
                            studentId = studentId,
                            email = email,
                            isMentor = true,
                            subjects = doc.get("unitsTaken") as? List<String> ?: emptyList()
                        )
                        
                        MentorProfile(
                            uid = studentId,
                            bio = doc.getString("bio") ?: "",
                            areasOfExpertise = (doc.get("expertise") as? List<String>) ?: emptyList(),
                            unitsTaken = (doc.get("unitsTaken") as? List<String>) ?: emptyList(),
                            availability = (doc.get("availability") as? List<Map<String, Any>>)?.map { slot ->
                                TimeSlot(
                                    dayOfWeek = (slot["dayOfWeek"] as? Long)?.toInt() ?: 1,
                                    startTime = (slot["startTime"] as? String) ?: "9:00",
                                    endTime = (slot["endTime"] as? String) ?: "17:00"
                                )
                            } ?: emptyList()
                        ) to user
                    } catch (e: Exception) {
                        Log.e("DiscoverPage", "Error parsing mentor doc", e)
                        null
                    }
                }
            } else {
                val studentDocs = db.collection("students").get().await()
                students = studentDocs.mapNotNull { doc ->
                    try {
                        val studentId = doc.getString("uid") ?: return@mapNotNull null
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val email = doc.getString("email") ?: return@mapNotNull null
                        
                        val user = User(
                            name = name,
                            studentId = studentId,
                            email = email,
                            isMentor = false,
                            subjects = doc.get("units") as? List<String> ?: emptyList()
                        )
                        
                        StudentProfile(
                            uid = studentId,
                            bio = doc.getString("bio") ?: "",
                            areasOfInterest = (doc.get("majors") as? List<String>) ?: emptyList(),
                            currentUnits = (doc.get("units") as? List<String>) ?: emptyList(),
                            academicGoals = doc.getString("academicGoals") ?: ""
                        ) to user
                    } catch (e: Exception) {
                        Log.e("DiscoverPage", "Error parsing student doc", e)
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DiscoverPage", "Error loading profiles", e)
            errorMessage = "Error loading profiles: ${e.message}"
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

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
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF009AC7))
                }
            }
            // Error message
            else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Unknown error occurred",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            // User List
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showMentors) {
                        items(mentors.filter { (mentor, user) ->
                            searchQuery.isEmpty() || 
                            user.name.contains(searchQuery, ignoreCase = true) ||
                            mentor.unitsTaken.any { it.contains(searchQuery, ignoreCase = true) } ||
                            mentor.areasOfExpertise.any { it.contains(searchQuery, ignoreCase = true) }
                        }) { (mentor, user) ->
                            MentorCard(mentor = mentor, user = user) {
                                val intent = Intent(context, ProfileViewPage::class.java).apply {
                                    putExtra("user", User(
                                        name = user.name,
                                        studentId = user.studentId,
                                        email = user.email,
                                        isMentor = true,
                                        subjects = user.subjects
                                    ))
                                }
                                context.startActivity(intent)
                            }
                        }
                    } else {
                        items(students.filter { (student, user) ->
                            searchQuery.isEmpty() || 
                            user.name.contains(searchQuery, ignoreCase = true) ||
                            student.currentUnits.any { it.contains(searchQuery, ignoreCase = true) } ||
                            student.areasOfInterest.any { it.contains(searchQuery, ignoreCase = true) }
                        }) { (student, user) ->
                            StudentCard(student = student, user = user) {
                                val intent = Intent(context, ProfileViewPage::class.java).apply {
                                    putExtra("user", User(
                                        name = user.name,
                                        studentId = user.studentId,
                                        email = user.email,
                                        isMentor = false,
                                        subjects = user.subjects
                                    ))
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MentorCard(mentor: MentorProfile, user: User, onClick: () -> Unit) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
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
                    text = user.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF002A5C)
                )
                Text(
                    text = mentor.unitsTaken.joinToString(", "),
                    fontSize = 14.sp,
                    color = Color(0xFF009AC7),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = mentor.bio,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Chat Button
            IconButton(
                onClick = { 
                    val intent = Intent(context, ChatPage::class.java).apply {
                        putExtra("user", User(
                            name = user.name,
                            studentId = user.studentId,
                            email = user.email,
                            isMentor = true,
                            subjects = user.subjects
                        ))
                        putExtra("source", "discover")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Chat",
                    tint = Color(0xFF009AC7)
                )
            }
        }
    }
}

@Composable
fun StudentCard(student: StudentProfile, user: User, onClick: () -> Unit) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
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
                    text = user.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF002A5C)
                )
                Text(
                    text = student.currentUnits.joinToString(", "),
                    fontSize = 14.sp,
                    color = Color(0xFF009AC7),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = student.bio,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Chat Button
            IconButton(
                onClick = { 
                    val intent = Intent(context, ChatPage::class.java).apply {
                        putExtra("user", User(
                            name = user.name,
                            studentId = user.studentId,
                            email = user.email,
                            isMentor = false,
                            subjects = user.subjects
                        ))
                        putExtra("source", "discover")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Chat",
                    tint = Color(0xFF009AC7)
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentPage: Int) {
    val context = LocalContext.current
    
    NavigationBar {
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
                // Already on Discover page, do nothing
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
                val intent = Intent(context, ChatHistoryPage::class.java)
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

