package com.example.macathon_monashmates.screens

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.example.macathon_monashmates.data.repository.FirebaseRepository
import com.example.macathon_monashmates.data.models.MentorProfile
import com.example.macathon_monashmates.data.models.StudentProfile
import com.example.macathon_monashmates.data.models.TimeSlot
import com.example.macathon_monashmates.utils.MonashBlue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "DebugDatabaseView"

class DebugDatabaseView : ComponentActivity() {
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DebugDatabaseContent(repository)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugDatabaseContent(repository: FirebaseRepository) {
    var mentors by remember { mutableStateOf<List<MentorProfile>>(emptyList()) }
    var students by remember { mutableStateOf<List<StudentProfile>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Function to add test data
    suspend fun addTestData() {
        try {
            Log.d(TAG, "Starting to add test data")
            
            // Add a test mentor
            val testMentor = MentorProfile(
                uid = "test_mentor_1",
                bio = "I am a test mentor specializing in Computer Science",
                areasOfExpertise = listOf("Data Science", "Machine Learning", "Android Development"),
                unitsTaken = listOf("FIT3077", "FIT3146", "FIT3047"),
                availability = listOf(
                    TimeSlot(
                        dayOfWeek = 1,  // Monday
                        startTime = "09:00",
                        endTime = "11:00"
                    ),
                    TimeSlot(
                        dayOfWeek = 3,  // Wednesday
                        startTime = "14:00",
                        endTime = "16:00"
                    )
                )
            )
            
            Log.d(TAG, "Adding test mentor: $testMentor")
            repository.createOrUpdateMentorProfile(testMentor)
            Log.d(TAG, "Successfully added test mentor")

            // Add a test student
            val testStudent = StudentProfile(
                uid = "test_student_1",
                bio = "I am a test student interested in AI",
                areasOfInterest = listOf("Artificial Intelligence", "Machine Learning"),
                currentUnits = listOf("FIT2004", "FIT2014", "FIT2099"),
                academicGoals = "I want to become a Machine Learning Engineer"
            )
            
            Log.d(TAG, "Adding test student: $testStudent")
            repository.createOrUpdateStudentProfile(testStudent)
            Log.d(TAG, "Successfully added test student")

            errorMessage = null
            Toast.makeText(context, "Test data added successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding test data", e)
            errorMessage = "Error: ${e.message}"
            Toast.makeText(context, "Error adding test data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        try {
            Log.d(TAG, "Loading initial data")
            repository.getAllMentors().collectLatest { mentorsList ->
                Log.d(TAG, "Received mentors: $mentorsList")
                mentors = mentorsList
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading mentors", e)
            errorMessage = "Error loading mentors: ${e.message}"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Error message display
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Mentors") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Students") }
            )
        }

        // Add Test Data Button
        Button(
            onClick = {
                scope.launch {
                    addTestData()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MonashBlue
            )
        ) {
            Text("Add Test Data")
        }

        when (selectedTab) {
            0 -> {
                if (mentors.isEmpty()) {
                    Text(
                        "No mentors found",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(mentors) { mentor ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Mentor ID: ${mentor.uid}")
                                Text("Bio: ${mentor.bio}")
                                Text("Expertise: ${mentor.areasOfExpertise.joinToString(", ")}")
                                Text("Units: ${mentor.unitsTaken.joinToString(", ")}")
                                Text("Available Times:")
                                mentor.availability.forEach { slot ->
                                    val day = when(slot.dayOfWeek) {
                                        1 -> "Monday"
                                        2 -> "Tuesday"
                                        3 -> "Wednesday"
                                        4 -> "Thursday"
                                        5 -> "Friday"
                                        6 -> "Saturday"
                                        7 -> "Sunday"
                                        else -> "Unknown"
                                    }
                                    Text("$day: ${slot.startTime} - ${slot.endTime}")
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                if (students.isEmpty()) {
                    Text(
                        "No students found",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(students) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Student ID: ${student.uid}")
                                Text("Bio: ${student.bio}")
                                Text("Interests: ${student.areasOfInterest.joinToString(", ")}")
                                Text("Current Units: ${student.currentUnits.joinToString(", ")}")
                                Text("Academic Goals: ${student.academicGoals}")
                            }
                        }
                    }
                }
            }
        }

        // Refresh button
        Button(
            onClick = {
                scope.launch {
                    try {
                        Log.d(TAG, "Refreshing data")
                        repository.getAllMentors().collectLatest { mentorsList ->
                            Log.d(TAG, "Refreshed mentors: $mentorsList")
                            mentors = mentorsList
                        }
                        repository.getAllStudents().collectLatest { studentsList ->
                            Log.d(TAG, "Refreshed students: $studentsList")
                            students = studentsList
                        }
                        errorMessage = null
                        Toast.makeText(context, "Data refreshed!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error refreshing data", e)
                        errorMessage = "Error refreshing data: ${e.message}"
                        Toast.makeText(context, "Error refreshing data: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Refresh Data")
        }
    }
} 