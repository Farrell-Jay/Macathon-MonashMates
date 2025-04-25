package com.example.macathon_monashmates.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macathon_monashmates.R
import com.example.macathon_monashmates.managers.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NewLoginPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewLoginScreen()
        }
    }
}

@Composable
fun NewLoginScreen() {
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val userManager = remember { UserManager(context) }
    val firestore = remember { Firebase.firestore }
    
    // Email validation function
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo and Title
        Spacer(modifier = Modifier.height(32.dp))
        Image(
            painter = painterResource(id = R.drawable.monashmates),
            contentDescription = "MonashMates Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 32.dp)
        )
        
        Text(
            text = "Welcome Back!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF003B5C),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Please login to continue",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Login Form
        OutlinedTextField(
            value = studentId,
            onValueChange = { 
                studentId = it
                errorMessage = null
            },
            label = { Text("Student ID") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Student ID",
                    tint = Color(0xFF003B5C)
                )
            },
            isError = errorMessage != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color(0xFF003B5C)
                )
            },
            isError = errorMessage != null,
            supportingText = {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Login Button
        Button(
            onClick = {
                // Validate that both fields are filled
                if (studentId.isEmpty() || email.isEmpty()) {
                    errorMessage = "Please enter both Student ID and Email"
                    return@Button
                }
                
                // Validate email format
                if (!isValidEmail(email)) {
                    errorMessage = "Please enter a valid email address"
                    return@Button
                }
                
                isLoading = true
                
                // First try mentors collection
                Toast.makeText(context, "Checking mentor collection for ID: $studentId", Toast.LENGTH_SHORT).show()
                firestore.collection("mentors")
                    .document(studentId)
                    .get()
                    .addOnSuccessListener { mentorDoc ->
                        if (mentorDoc.exists()) {
                            val mentorEmail = mentorDoc.getString("email")
                            val mentorUid = mentorDoc.getString("uid")
                            
                            // Check if studentId matches uid
                            if (mentorUid != studentId) {
                                Toast.makeText(context, "Mentor UID mismatch: $mentorUid vs $studentId", Toast.LENGTH_SHORT).show()
                                errorMessage = "Invalid Student ID"
                                isLoading = false
                                return@addOnSuccessListener
                            }
                            
                            // Check if email matches exactly
                            if (mentorEmail != email) {
                                errorMessage = "Invalid Email"
                                isLoading = false
                                return@addOnSuccessListener
                            }
                            
                            // Mentor login successful
                            val user = userManager.getUserByStudentId(studentId)
                            if (user != null) {
                                userManager.setCurrentUser(user)
                                Toast.makeText(context, "Login successful! Redirecting to Home Page", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, HomePage::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as? Activity)?.finish()
                            } else {
                                errorMessage = "Invalid Student ID or Email"
                                isLoading = false
                            }
                        } else {
                            // If not found in mentors, try students collection
                            checkStudentCollection(
                                studentId = studentId,
                                email = email,
                                context = context,
                                userManager = userManager,
                                onError = { error ->
                                    errorMessage = error
                                },
                                onLoadingChange = { loading ->
                                    isLoading = loading
                                }
                            )
                        }
                    }
                    .addOnFailureListener { _ ->
                        isLoading = false
                        errorMessage = "Error connecting to server. Please try again."
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && studentId.isNotEmpty() && email.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
        
        // Sign Up Link
        TextButton(
            onClick = {
                val intent = Intent(context, SignUpPage::class.java)
                context.startActivity(intent)
                (context as Activity).finish()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Don't have an account? Sign Up",
                color = Color(0xFF003B5C)
            )
        }
    }
}

// Helper function to check students collection
private fun checkStudentCollection(
    studentId: String,
    email: String,
    context: android.content.Context,
    userManager: UserManager,
    onError: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    Toast.makeText(context, "Checking student collection for ID: $studentId", Toast.LENGTH_SHORT).show()
    Firebase.firestore.collection("students")
        .document(studentId)
        .get()
        .addOnSuccessListener { studentDoc ->
            onLoadingChange(false)
            if (studentDoc.exists()) {
                val studentEmail = studentDoc.getString("email")
                val studentUid = studentDoc.getString("uid")
                
                // Check if studentId matches uid
                if (studentUid != studentId) {
                    Toast.makeText(context, "Student UID mismatch: $studentUid vs $studentId", Toast.LENGTH_SHORT).show()
                    onError("Invalid Student ID")
                    return@addOnSuccessListener
                }
                
                // Check if email matches exactly
                if (studentEmail != email) {
                    onError("Invalid Email")
                    return@addOnSuccessListener
                }
                
                // Student login successful
                val user = userManager.getUserByStudentId(studentId)
                if (user != null) {
                    userManager.setCurrentUser(user)
                    Toast.makeText(context, "Login successful! Redirecting to Home Page", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, HomePage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                } else {
                    onError("Invalid Student ID or Email")
                }
            } else {
                onError("Invalid Student ID or Email")
            }
        }
        .addOnFailureListener { _ ->
            onLoadingChange(false)
            onError("Error connecting to server. Please try again.")
        }
} 