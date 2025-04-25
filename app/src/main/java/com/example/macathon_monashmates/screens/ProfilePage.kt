package com.example.macathon_monashmates.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.macathon_monashmates.R
import com.example.macathon_monashmates.models.User
import com.example.macathon_monashmates.managers.UserManager
import com.example.macathon_monashmates.utils.MonashBlue
import com.example.macathon_monashmates.utils.MonashLightBlue
import com.example.macathon_monashmates.utils.MonashDarkBlue
import com.example.macathon_monashmates.data.repository.FirebaseRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class ProfilePage : ComponentActivity() {
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen()
        }
    }
}

@Composable
fun StatColumn(number: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MonashBlue
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val userManager = remember { UserManager(context) }
    val currentUser = userManager.getCurrentUser()
    val db = FirebaseFirestore.getInstance()
    
    var bio by remember { mutableStateOf("") }
    var academicGoals by remember { mutableStateOf("") }
    var majors by remember { mutableStateOf(listOf<String>()) }
    var expertiseAreas by remember { mutableStateOf(listOf<String>()) }
    var expertiseLevels by remember { mutableStateOf(mapOf<String, String>()) }
    var subjects by remember { mutableStateOf(listOf<String>()) }
    var timeSlots by remember { mutableStateOf(listOf<String>()) }
    
    // Load data from Firestore
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            try {
                if (currentUser.isMentor) {
                    // Load mentor profile
                    val mentorDoc = db.collection("mentors")
                        .document(currentUser.studentId)
                        .get()
                        .await()
                        
                    if (mentorDoc.exists()) {
                        bio = mentorDoc.getString("bio") ?: ""
                        expertiseAreas = (mentorDoc.get("expertise") as? List<String>) ?: listOf()
                        subjects = (mentorDoc.get("unitsTaken") as? List<String>) ?: listOf()
                        val availabilityList = mentorDoc.get("availability") as? List<Map<String, Any>>
                        timeSlots = availabilityList?.map { slot ->
                            val day = when (slot["dayOfWeek"] as Long) {
                                1L -> "Monday"
                                2L -> "Tuesday"
                                3L -> "Wednesday"
                                4L -> "Thursday"
                                5L -> "Friday"
                                6L -> "Saturday"
                                7L -> "Sunday"
                                else -> "Unknown"
                            }
                            "$day ${slot["startTime"]} - ${slot["endTime"]}"
                        } ?: listOf()
                    }
                } else {
                    // Load student profile
                    val studentDoc = db.collection("students")
                        .document(currentUser.studentId)
                        .get()
                        .await()
                        
                    if (studentDoc.exists()) {
                        bio = studentDoc.getString("bio") ?: ""
                        academicGoals = studentDoc.getString("academicGoals") ?: ""
                        majors = (studentDoc.get("majors") as? List<String>) ?: listOf()
                        subjects = (studentDoc.get("units") as? List<String>) ?: listOf()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfilePage", "Error loading profile data", e)
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MonashBlue,
                            MonashBlue.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                TopBar(context)
            }

            item {
                ProfileHeader(currentUser)
            }

            item {
                MainContent(
                    currentUser = currentUser,
                    bio = bio,
                    academicGoals = academicGoals,
                    expertiseAreas = expertiseAreas,
                    expertiseLevels = expertiseLevels,
                    subjects = subjects,
                    timeSlots = timeSlots,
                    majors = majors
                )
            }
        }
    }
}

@Composable
private fun TopBar(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { 
                val intent = Intent(context, HomePage::class.java)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = "PROFILE",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = { /* TODO: Settings */ }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ProfileHeader(currentUser: User?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.default_profile),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentUser?.name ?: "",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = if (currentUser?.isMentor == true) "Mentor" else "Student",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: Edit Profile */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(text = "Edit Profile", color = MonashBlue)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainContent(
    currentUser: User?,
    bio: String,
    academicGoals: String,
    expertiseAreas: List<String>,
    expertiseLevels: Map<String, String>,
    subjects: List<String>,
    timeSlots: List<String>,
    majors: List<String>
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (currentUser?.isMentor == true) {
                    StatColumn(
                        number = subjects.size.toString(),
                        label = "Subjects"
                    )
                    StatColumn(
                        number = timeSlots.size.toString(),
                        label = "Time Slots"
                    )
                    StatColumn(
                        number = expertiseAreas.size.toString(),
                        label = "Expertise"
                    )
                } else {
                    StatColumn(
                        number = subjects.size.toString(),
                        label = "Units"
                    )
                    StatColumn(
                        number = majors.size.toString(),
                        label = "Areas of Interest"
                    )
                }
            }

            Divider(modifier = Modifier.padding(bottom = 16.dp))

            // About Me Section
            if (bio.isNotEmpty()) {
                Text(
                    text = "About Me",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = bio,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Subject/Units Section
            if (subjects.isNotEmpty()) {
                Text(
                    text = if (currentUser?.isMentor == true) "Units I Can Mentor" else "Units Taken",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subject ->
                        Surface(
                            color = MonashLightBlue,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = subject,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MonashBlue
                            )
                        }
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }

            if (currentUser?.isMentor == true) {
                // Mentor-specific sections
                if (expertiseAreas.isNotEmpty()) {
                    ExpertiseSection(expertiseAreas, expertiseLevels)
                }
                if (timeSlots.isNotEmpty()) {
                    AvailabilitySection(timeSlots)
                }
            } else {
                // Student-specific sections
                if (majors.isNotEmpty()) {
                    Text(
                        text = "Areas of Interest",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MonashBlue,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        majors.forEach { major ->
                            Surface(
                                color = MonashLightBlue,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = major,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = MonashBlue
                                )
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }

                if (academicGoals.isNotEmpty()) {
                    AcademicGoalsSection(academicGoals)
                }

                // Edit Profile Button for Students
                Button(
                    onClick = {
                        val intent = Intent(context, StudentInterestPage::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MonashBlue)
                ) {
                    Text("Edit Profile")
                }
            }
        }
    }
}

@Composable
private fun ExpertiseSection(expertiseAreas: List<String>, expertiseLevels: Map<String, String>) {
    Text(
        text = "Areas of Expertise",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MonashBlue,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    expertiseAreas.forEach { area ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MonashLightBlue
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = area,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue
                )
                if (expertiseLevels.containsKey(area)) {
                    Text(
                        text = expertiseLevels[area] ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
    Divider(modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
private fun AvailabilitySection(timeSlots: List<String>) {
    Text(
        text = "Weekly Availability",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MonashBlue,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    timeSlots.forEach { slot ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MonashLightBlue
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = slot,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp),
                color = MonashBlue
            )
        }
    }
    Divider(modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
private fun AcademicGoalsSection(academicGoals: String) {
    Text(
        text = "Academic Goals",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MonashBlue,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        text = academicGoals,
        fontSize = 16.sp,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 24.dp)
    )
    Divider(modifier = Modifier.padding(bottom = 16.dp))
}

