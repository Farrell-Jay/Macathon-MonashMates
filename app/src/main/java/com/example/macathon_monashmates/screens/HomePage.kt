package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.macathon_monashmates.models.User
import com.example.macathon_monashmates.data.models.MentorProfile
import com.example.macathon_monashmates.data.models.StudentProfile

class HomePage : ComponentActivity() {
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
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Simulated data - in a real app, this would come from Firebase
    val recommendedMentors = remember {
        listOf(
            MentorRecommendation(
                name = "Jane Smith",
                expertise = listOf("Computer Science", "Machine Learning"),
                bio = "PhD student specializing in AI systems",
                rating = 4.9f
            ),
            MentorRecommendation(
                name = "Michael Brown",
                expertise = listOf("Engineering", "Mathematics"),
                bio = "3rd year Engineering student with tutoring experience",
                rating = 4.7f
            ),
            MentorRecommendation(
                name = "Sarah Johnson",
                expertise = listOf("Business", "Economics"),
                bio = "Business graduate with industry experience",
                rating = 4.8f
            )
        )
    }
    
    val recommendedStudents = remember {
        listOf(
            StudentRecommendation(
                name = "Alex Turner",
                interests = listOf("Mobile App Development", "Web Design"),
                bio = "First year CS student looking for coding mentorship",
                year = "1st Year"
            ),
            StudentRecommendation(
                name = "Emily Chen",
                interests = listOf("Data Science", "Statistics"),
                bio = "Seeking help with advanced statistics concepts",
                year = "2nd Year"
            ),
            StudentRecommendation(
                name = "Carlos Rodriguez",
                interests = listOf("Cybersecurity", "Networking"),
                bio = "Passionate about security and looking for guidance",
                year = "3rd Year"
            )
        )
    }
    
    // State to control recommendation dialogs 
    var showAllMentors by remember { mutableStateOf(false) }
    var showAllStudents by remember { mutableStateOf(false) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { 
                        // Already on Home page
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
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, DiscoverPage::class.java)
                        context.startActivity(intent)
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
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, ChatHistoryPage::class.java)
                        context.startActivity(intent)
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
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, ProfilePage::class.java)
                        context.startActivity(intent)
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F4F4))
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo at the top
            Image(
                painter = painterResource(id = R.drawable.monashmates),
                contentDescription = "MonashMates Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
            
            // Welcome Message
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to MonashMates!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002A5C),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Connect with mentors and students to enhance your academic journey",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Button(
                        onClick = { 
                            val intent = Intent(context, DiscoverPage::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009AC7))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse All Connections")
                    }
                }
            }
            
            // Recommended Mentors Section
            SectionTitle(
                title = "Recommended Mentors", 
                icon = Icons.Default.Person,
                onViewAllClick = { showAllMentors = true }
            )
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(recommendedMentors) { mentor ->
                    MentorCard(
                        name = mentor.name,
                        expertise = mentor.expertise,
                        bio = mentor.bio,
                        rating = mentor.rating,
                        onClick = {
                            // In a real app, this would navigate to the mentor's profile
                            // For now, just navigate to DiscoverPage
                            val intent = Intent(context, DiscoverPage::class.java)
                            context.startActivity(intent)
                        }
                    )
                }
            }
            
            // Recommended Students Section
            SectionTitle(
                title = "Recommended Students", 
                icon = Icons.Default.School,
                onViewAllClick = { showAllStudents = true }
            )
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(recommendedStudents) { student ->
                    StudentCard(
                        name = student.name,
                        interests = student.interests,
                        bio = student.bio,
                        year = student.year,
                        onClick = {
                            // In a real app, this would navigate to the student's profile
                            // For now, just navigate to DiscoverPage
                            val intent = Intent(context, DiscoverPage::class.java)
                            context.startActivity(intent)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Show "View All Mentors" Dialog
        if (showAllMentors) {
            AllRecommendationsDialog(
                title = "All Recommended Mentors",
                onDismiss = { showAllMentors = false }
            ) {
                recommendedMentors.forEach { mentor ->
                    MentorCard(
                        name = mentor.name,
                        expertise = mentor.expertise,
                        bio = mentor.bio,
                        rating = mentor.rating,
                        onClick = {
                            showAllMentors = false
                            val intent = Intent(context, DiscoverPage::class.java)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        
        // Show "View All Students" Dialog
        if (showAllStudents) {
            AllRecommendationsDialog(
                title = "All Recommended Students",
                onDismiss = { showAllStudents = false }
            ) {
                recommendedStudents.forEach { student ->
                    StudentCard(
                        name = student.name,
                        interests = student.interests,
                        bio = student.bio,
                        year = student.year,
                        onClick = {
                            showAllStudents = false
                            val intent = Intent(context, DiscoverPage::class.java)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF002A5C),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF002A5C),
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = onViewAllClick
        ) {
            Text(
                text = "View All",
                color = Color(0xFF009AC7),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AllRecommendationsDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF002A5C)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp)
            ) {
                content()
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = Color(0xFF009AC7)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun MentorCard(
    name: String,
    expertise: List<String>,
    bio: String,
    rating: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Profile Image & Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF009AC7))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)
                    )
                }
                
                // Name & Rating
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002A5C)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = rating.toString(),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            
            // Expertise
            Text(
                text = "Expertise:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF002A5C),
                modifier = Modifier.padding(top = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                expertise.take(2).forEach { subject ->
                    Surface(
                        modifier = Modifier.height(26.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF009AC7).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = subject,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF009AC7),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (expertise.size > 2) {
                    Text(
                        text = "+${expertise.size - 2}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            
            // Bio
            Text(
                text = bio,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 2
            )
            
            // Chat Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009AC7))
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Connect",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connect",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun StudentCard(
    name: String,
    interests: List<String>,
    bio: String,
    year: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Profile Image & Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF009AC7))
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Student",
                        tint = Color.White,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)
                    )
                }
                
                // Name & Year
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002A5C)
                    )
                    Text(
                        text = year,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Interests
            Text(
                text = "Interests:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF002A5C),
                modifier = Modifier.padding(top = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                interests.take(2).forEach { interest ->
                    Surface(
                        modifier = Modifier.height(26.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF009AC7).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = interest,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF009AC7),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (interests.size > 2) {
                    Text(
                        text = "+${interests.size - 2}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            
            // Bio
            Text(
                text = bio,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 2
            )
            
            // Mentor Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009AC7))
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Mentor",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mentor",
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Data classes for the recommendations
data class MentorRecommendation(
    val name: String,
    val expertise: List<String>,
    val bio: String,
    val rating: Float
)

data class StudentRecommendation(
    val name: String,
    val interests: List<String>,
    val bio: String,
    val year: String
) 