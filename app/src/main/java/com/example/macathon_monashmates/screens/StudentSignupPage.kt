package com.example.macathon_monashmates.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import com.example.macathon_monashmates.managers.UserManager
import com.example.macathon_monashmates.models.User

private const val TAG = "StudentSignupPage"

class StudentSignupPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudentSignupScreen()
        }
    }
}

@Composable
fun StudentSignupScreen() {
    var fullName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var studentIdError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val userManager = remember { UserManager(context) }
    
    // Function to validate full name
    fun validateFullName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z\\s]*$"))
    }
    
    // Function to validate student ID
    fun validateStudentId(id: String): Boolean {
        return id.matches(Regex("^\\d{8}$"))
    }
    
    // Function to get student ID error message
    fun getStudentIdError(id: String): String? {
        return when {
            !id.matches(Regex("^\\d*$")) -> "Numbers only"
            id.length < 8 -> "${8 - id.length} numbers remaining"
            else -> null
        }
    }
    
    // Function to validate Monash email
    fun validateMonashEmail(email: String): Boolean {
        return email.endsWith("@student.monash.edu")
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
            text = "Student Sign Up",
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
                    onValueChange = { 
                        fullName = it
                        fullNameError = if (it.isNotEmpty() && !validateFullName(it)) {
                            "Alphabets Only"
                        } else {
                            null
                        }
                    },
                    label = { Text("Full Name") },
                    isError = fullNameError != null,
                    supportingText = {
                        if (fullNameError != null) {
                            Text(
                                text = fullNameError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { 
                        studentId = it
                        studentIdError = getStudentIdError(it)
                    },
                    label = { Text("Student ID") },
                    isError = studentIdError != null,
                    supportingText = {
                        if (studentIdError != null) {
                            Text(
                                text = studentIdError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = if (it.isNotEmpty() && !validateMonashEmail(it)) {
                            "Please enter a valid Monash email (e.g., shoq0003@student.monash.edu)"
                        } else {
                            null
                        }
                    },
                    label = { Text("Email") },
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) {
                            Text(
                                text = emailError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Button(
                    onClick = { 
                        if (validateFullName(fullName) && validateStudentId(studentId) && validateMonashEmail(email)) {
                            Log.d(TAG, "Starting signup process...")
                            val userId = UUID.randomUUID().toString()
                            
                            // Create basic user document
                            val userDoc = hashMapOf(
                                "uid" to userId,
                                "name" to fullName,
                                "email" to email,
                                "studentId" to studentId,
                                "isMentor" to false
                            )
                            
                            // Create basic student profile
                            val studentProfile = hashMapOf(
                                "uid" to userId,
                                "name" to fullName,
                                "email" to email,
                                "studentId" to studentId,
                                "bio" to "",
                                "areasOfInterest" to listOf<String>(),
                                "currentUnits" to listOf<String>(),
                                "academicGoals" to ""
                            )
                            
                            Log.d(TAG, "Creating user document with ID: $userId")
                            // Save to users collection
                            firestore.collection("users")
                                .document(userId)
                                .set(userDoc)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User document created successfully with data: $userDoc")
                                    
                                    // Save to students collection
                                    firestore.collection("students")
                                        .document(userId)
                                        .set(studentProfile)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Student profile created successfully with data: $studentProfile")
                                            
                                            // Create and save local User object
                                            val user = User(
                                                name = fullName,
                                                studentId = studentId,
                                                email = email,
                                                isMentor = false,
                                                subjects = emptyList()
                                            )
                                            userManager.saveUser(user)
                                            userManager.setCurrentUser(user)
                                            
                                            Toast.makeText(context, "Sign up successful! Complete your profile.", Toast.LENGTH_SHORT).show()
                                            
                                            val intent = Intent(context, StudentInterestPage::class.java)
                                            intent.putExtra("userId", userId)
                                            context.startActivity(intent)
                                            (context as ComponentActivity).finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error creating student profile", e)
                                            // Cleanup if student profile creation fails
                                            firestore.collection("users").document(userId).delete()
                                            Toast.makeText(context, "Error creating student profile: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error creating user", e)
                                    Toast.makeText(context, "Error creating user: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = fullName.isNotEmpty() && 
                             studentId.isNotEmpty() && 
                             email.isNotEmpty() && 
                             fullNameError == null &&
                             studentIdError == null &&
                             emailError == null &&
                             studentId.length == 8 &&
                             validateMonashEmail(email)
                ) {
                    Text("Sign Up")
                }
            }
        }
    }
} 