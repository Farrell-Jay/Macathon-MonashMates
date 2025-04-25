package com.example.macathon_monashmates.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.macathon_monashmates.utils.SubjectReader
import com.example.macathon_monashmates.utils.Subject
import com.example.macathon_monashmates.models.User
import com.example.macathon_monashmates.managers.UserManager
import com.example.macathon_monashmates.utils.MonashBlue
import com.example.macathon_monashmates.utils.MonashLightBlue
import com.example.macathon_monashmates.utils.MonashDarkBlue
import kotlinx.coroutines.launch

class StudentInterestPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudentInterestScreen()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentInterestScreen() {
    val subjects = remember { mutableStateListOf<Subject>() }
    var selectedSubjects by remember { mutableStateOf(setOf<Subject>()) }
    var selectedMajors by remember { mutableStateOf(setOf<String>()) }
    var showSubjectDialog by remember { mutableStateOf(false) }
    var showMajorDialog by remember { mutableStateOf(false) }
    var academicGoals by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userManager = remember { UserManager(context) }
    val currentUser = userManager.getCurrentUser()
    
    val majors = listOf(
        "Computer Science",
        "Data Science",
        "Software Engineering",
        "Cybersecurity",
        "Information Technology",
        "Artificial Intelligence",
        "Business Information Systems",
        "Computer Systems",
        "Games Development",
        "Mobile and Web Development"
    )
    
    LaunchedEffect(Unit) {
        scope.launch {
            subjects.addAll(SubjectReader.readSubjects(context))
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button
        IconButton(
            onClick = { 
                val intent = Intent(context, StudentSignupPage::class.java)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MonashBlue
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Details Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MonashLightBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Hi ${currentUser?.name ?: "there"}!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonashBlue,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Please complete your profile to help us find the best mentors for you.",
                            fontSize = 16.sp,
                            color = MonashDarkBlue
                        )
                    }
                }
            }
            
            // Bio Section
            item {
                Text(
                    text = "Student Profile Setup",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Short Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tell us about yourself and your learning style") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonashBlue,
                        unfocusedBorderColor = MonashBlue.copy(alpha = 0.5f),
                        focusedLabelColor = MonashBlue,
                        unfocusedLabelColor = MonashBlue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            
            // Areas of Interest (Majors) Section
            item {
                Text(
                    text = "Areas of Interest",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                OutlinedButton(
                    onClick = { showMajorDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MonashBlue
                    ),
                    border = BorderStroke(1.dp, MonashBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Areas of Interest")
                }
            }
            
            // Selected Majors
            if (selectedMajors.isNotEmpty()) {
                items(selectedMajors.toList()) { major ->
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
                            text = major,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            
            // Units Section
            item {
                Text(
                    text = "Units Taken",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                OutlinedButton(
                    onClick = { showSubjectDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MonashBlue
                    ),
                    border = BorderStroke(1.dp, MonashBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Units")
                }
            }
            
            // Selected Units
            if (selectedSubjects.isNotEmpty()) {
                items(selectedSubjects.toList()) { subject ->
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
                            text = subject.toString(),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            
            // Academic Goals Section
            item {
                Text(
                    text = "Academic Goals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = academicGoals,
                    onValueChange = { academicGoals = it },
                    label = { Text("Your Academic Goals") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Describe your academic goals and what you hope to achieve") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonashBlue,
                        unfocusedBorderColor = MonashBlue.copy(alpha = 0.5f),
                        focusedLabelColor = MonashBlue,
                        unfocusedLabelColor = MonashBlue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            
            // Complete Profile Button
            item {
                Button(
                    onClick = {
                        if (currentUser != null) {
                            // Update user with new information
                            val updatedUser = currentUser.copy(
                                subjects = selectedSubjects.map { it.toString() }
                            )
                            
                            // Save updated user
                            userManager.saveUser(updatedUser)
                            userManager.setCurrentUser(updatedUser)
                            
                            // Save additional information in SharedPreferences
                            val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("${currentUser.studentId}_bio", bio)
                            editor.putString("${currentUser.studentId}_academicGoals", academicGoals)
                            editor.putString("${currentUser.studentId}_majors", selectedMajors.joinToString(","))
                            editor.apply()
                            
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            
                            // Redirect to home page
                            val intent = Intent(context, HomePage::class.java)
                            context.startActivity(intent)
                            (context as ComponentActivity).finish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MonashBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = selectedSubjects.isNotEmpty() && 
                             selectedMajors.isNotEmpty() && 
                             academicGoals.isNotEmpty() && 
                             bio.isNotEmpty()
                ) {
                    Text("Complete Profile")
                }
            }
        }
    }
    
    if (showSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showSubjectDialog = false },
            title = { Text("Select Units") },
            text = {
                Column {
                    subjects.forEach { subject ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = subject in selectedSubjects,
                                onCheckedChange = { checked ->
                                    selectedSubjects = if (checked) {
                                        selectedSubjects + subject
                                    } else {
                                        selectedSubjects - subject
                                    }
                                }
                            )
                            Text(subject.toString())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSubjectDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
    
    if (showMajorDialog) {
        AlertDialog(
            onDismissRequest = { showMajorDialog = false },
            title = { Text("Select Areas of Interest") },
            text = {
                Column {
                    majors.forEach { major ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = major in selectedMajors,
                                onCheckedChange = { checked ->
                                    selectedMajors = if (checked) {
                                        selectedMajors + major
                                    } else {
                                        selectedMajors - major
                                    }
                                }
                            )
                            Text(major)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMajorDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
} 