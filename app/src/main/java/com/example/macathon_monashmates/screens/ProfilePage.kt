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

class ProfilePage : ComponentActivity() {
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
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    
    val bio = remember { 
        mutableStateOf(sharedPreferences.getString("${currentUser?.studentId}_bio", "") ?: "") 
    }
    val academicGoals = remember { 
        mutableStateOf(sharedPreferences.getString("${currentUser?.studentId}_academicGoals", "") ?: "") 
    }
    val majors = remember { 
        mutableStateOf(sharedPreferences.getString("${currentUser?.studentId}_majors", "")?.split(",") ?: emptyList()) 
    }
    
    // Read expertise areas and levels
    val expertiseAreas = remember {
        mutableStateOf(sharedPreferences.getString("${currentUser?.studentId}_expertiseAreas", "")?.split("|||") ?: emptyList())
    }
    val expertiseLevels = remember {
        val levelsStr = sharedPreferences.getString("${currentUser?.studentId}_expertiseLevels", "")
        val levelsMap = if (levelsStr.isNullOrEmpty()) {
            mapOf()
        } else {
            levelsStr.split("|||").associate { 
                val (area, level) = it.split("::")
                area to level
            }
        }
        mutableStateOf(levelsMap)
    }
    
    // Read subjects
    val subjects = remember {
        mutableStateOf(sharedPreferences.getString("${currentUser?.studentId}_subjects", "")?.split("|||") ?: emptyList())
    }
    
    val timeSlots = remember { 
        val rawTimeSlots = sharedPreferences.getString("${currentUser?.studentId}_timeSlots", null)
        mutableStateOf(rawTimeSlots?.split("|||") ?: emptyList())
    }

    // Debug logging
    LaunchedEffect(Unit) {
        println("Debug - Loaded expertise areas: ${expertiseAreas.value}")
        println("Debug - Loaded expertise levels: ${expertiseLevels.value}")
        println("Debug - Loaded subjects: ${subjects.value}")
        println("Debug - Loaded time slots: ${timeSlots.value}")
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
                    bio = bio.value,
                    academicGoals = academicGoals.value,
                    expertiseAreas = expertiseAreas.value,
                    expertiseLevels = expertiseLevels.value,
                    subjects = subjects.value,
                    timeSlots = timeSlots.value
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
    timeSlots: List<String>
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    
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
            // Stats Row at the top
            val studentSubjects = if (currentUser?.isMentor == false) {
                sharedPreferences.getString("${currentUser.studentId}_subjects", "")
                    ?.split("|||")
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
            } else {
                subjects
            }

            val majorsList = if (currentUser?.isMentor == false) {
                sharedPreferences.getString("${currentUser.studentId}_majors", "")
                    ?.split(",")
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
            } else {
                emptyList()
            }

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
                        number = (timeSlots.size / 3).toString(),
                        label = "Time Slots"
                    )
                    StatColumn(
                        number = expertiseAreas.size.toString(),
                        label = "Expertise"
                    )
                } else {
                    StatColumn(
                        number = studentSubjects.size.toString(),
                        label = "Units"
                    )
                    StatColumn(
                        number = majorsList.size.toString(),
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
            if (studentSubjects.isNotEmpty()) {
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
                    studentSubjects.forEach { subject ->
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
                if (majorsList.isNotEmpty()) {
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
                        majorsList.forEach { major ->
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
    Divider(modifier = Modifier.padding(vertical = 16.dp))
    Text(
        text = "Areas of Expertise",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MonashBlue,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    expertiseAreas.forEach { area ->
        ExpertiseCard(area, expertiseLevels[area] ?: "Not specified")
    }
}

@Composable
private fun ExpertiseCard(area: String, level: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MonashLightBlue),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = area,
                fontSize = 16.sp,
                color = MonashBlue,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MonashBlue,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = level,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AvailabilitySection(timeSlots: List<String>) {
    Divider(modifier = Modifier.padding(vertical = 16.dp))
    Text(
        text = "Weekly Availability",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MonashBlue,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    // Debug logging
    println("Debug - AvailabilitySection received time slots: $timeSlots")
    
    val groupedSlots = remember(timeSlots) {
        timeSlots
            .chunked(3)  // Group into sets of 3 (day, start time, end time)
            .mapNotNull { chunk ->
                try {
                    if (chunk.size == 3) {
                        Triple(chunk[0], chunk[1], chunk[2])
                    } else null
                } catch (e: Exception) {
                    println("Debug - Error parsing time slot: $chunk")
                    println("Debug - Error: ${e.message}")
                    null
                }
            }
            .groupBy { it.first }
    }

    println("Debug - Grouped slots: $groupedSlots")

    if (groupedSlots.isEmpty()) {
        Text(
            text = "No availability slots added yet",
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        groupedSlots.forEach { (day, slots) ->
            AvailabilityCard(day, slots)
        }
    }
}

@Composable
private fun AvailabilityCard(day: String, slots: List<Triple<String, String, String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MonashLightBlue),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = day,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MonashBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            slots.forEach { (_, startTime, endTime) ->
                Text(
                    text = "$startTime - $endTime",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AcademicGoalsSection(academicGoals: String) {
    Divider(modifier = Modifier.padding(vertical = 16.dp))
    Text(
        text = "Academic Goals",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MonashBlue,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    Text(
        text = academicGoals,
        fontSize = 16.sp,
        color = Color.Gray
    )
}

