package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.macathon_monashmates.utils.SubjectReader
import com.example.macathon_monashmates.utils.Subject
import com.example.macathon_monashmates.models.User
import com.example.macathon_monashmates.managers.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
class MentorSignupPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentorSignupScreen()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MentorSignupScreen() {
    var fullName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    val subjects = remember { mutableStateListOf<Subject>() }
    var selectedSubjects by remember { mutableStateOf(setOf<Subject>()) }
    var showSubjectDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userManager = remember { UserManager(context) }
    val db = remember { FirebaseFirestore.getInstance() }
    
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
                val intent = Intent(context, SignUpPage::class.java)
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
        
        Text(
            text = "Mentor Sign Up",
            fontSize = 24.sp,
            fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Button(
                    onClick = { 
                        // Generate unique ID for the user
                        val userId = UUID.randomUUID().toString()
                        
                        // Create basic user document
                        val userDoc = hashMapOf(
                            "uid" to userId,
                            "name" to fullName,
                            "studentId" to studentId,
                            "email" to email,
                            "isMentor" to true
                        )
                        
                        // Create basic mentor profile
                        val mentorDoc = hashMapOf(
                            "uid" to userId,
                            "name" to fullName,
                            "studentId" to studentId,
                            "email" to email,
                            "expertise" to listOf<String>(),
                            "unitsTaken" to listOf<String>(),
                            "availability" to listOf<Map<String, Any>>(),
                            "bio" to ""
                        )
                        
                        // Save to users collection
                        db.collection("users").document(userId)
                            .set(userDoc)
                            .addOnSuccessListener {
                                Log.d("MentorSignup", "User document created successfully with data: $userDoc")
                                
                                // Save to mentors collection
                                db.collection("mentors").document(userId)
                                    .set(mentorDoc)
                                    .addOnSuccessListener {
                                        Log.d("MentorSignup", "Mentor profile created successfully with data: $mentorDoc")
                                        
                                        // Create and save local User object
                                        val user = User(
                                            name = fullName,
                                            studentId = studentId,
                                            email = email,
                                            isMentor = true,
                                            subjects = emptyList()
                                        )
                                        userManager.saveUser(user)
                                        userManager.setCurrentUser(user)
                                        
                                        Toast.makeText(context, "Sign up successful! Complete your profile.", Toast.LENGTH_SHORT).show()
                                        
                                        val intent = Intent(context, MentorExpertisePage::class.java)
                                        intent.putExtra("userId", userId)
                                        context.startActivity(intent)
                                        (context as ComponentActivity).finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("MentorSignup", "Error creating mentor profile", e)
                                        // Cleanup if mentor profile creation fails
                                        db.collection("users").document(userId).delete()
                                        Toast.makeText(context, "Error creating mentor profile: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("MentorSignup", "Error creating user", e)
                                Toast.makeText(context, "Error creating user: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = fullName.isNotEmpty() && studentId.isNotEmpty() && email.isNotEmpty()
                ) {
                    Text("Sign Up")
                }
            }
        }
    }
    
    if (showSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showSubjectDialog = false },
            title = { Text("Select Areas of Expertise") },
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
} 