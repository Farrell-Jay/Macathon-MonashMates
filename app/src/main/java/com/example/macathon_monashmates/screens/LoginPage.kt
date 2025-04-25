package com.example.macathon_monashmates.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macathon_monashmates.managers.UserManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
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
            .padding(16.dp)
    ) {
        // Back button
        IconButton(
            onClick = {
                val intent = Intent(context, SignUpPage::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = "Welcome Back!",
            fontSize = 24.sp,
            fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Please login to continue",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = studentId,
            onValueChange = {
                studentId = it
                errorMessage = null
            },
            label = { Text("Student ID") },
            placeholder = { Text("Enter your Student ID") },
            isError = errorMessage != null,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            placeholder = { Text("Enter your Email") },
            isError = errorMessage != null,
            singleLine = true,
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
                .padding(bottom = 16.dp)
        )

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
                                Toast.makeText(context, "Mentor login successful! Redirecting to MentorExpertisePage", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, MentorExpertisePage::class.java)
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

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = {
                    val intent = Intent(context, SignUpPage::class.java)
                    context.startActivity(intent)
                }
            ) {
                Text("Sign Up")
            }
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
                    Toast.makeText(context, "Student login successful! Redirecting to StudentInterestPage", Toast.LENGTH_SHORT).show()
                    // Navigate to StudentInterestPage
                    val intent = Intent(context, StudentInterestPage::class.java)
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
