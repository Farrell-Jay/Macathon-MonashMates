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
    var mentorId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var mentorIdError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    val subjects = remember { mutableStateListOf<Subject>() }
    var selectedSubjects by remember { mutableStateOf(setOf<Subject>()) }
    var showSubjectDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userManager = remember { UserManager(context) }
    val db = remember { FirebaseFirestore.getInstance() }
    
    // Function to validate full name
    fun validateFullName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z\\s]*$"))
    }
    
    // Function to validate mentor ID
    fun validateMentorId(id: String): Boolean {
        return id.matches(Regex("^\\d{8}$"))
    }
    
    // Function to get mentor ID error message
    fun getMentorIdError(id: String): String? {
        return when {
            !id.matches(Regex("^\\d*$")) -> "Numbers only"
            id.length < 8 -> "${8 - id.length} numbers remaining"
            else -> null
        }
    }
    
    // Function to validate Monash email
    fun validateMonashEmail(email: String): Boolean {
        return email.endsWith("@student.monash.edu") || email.endsWith("@monash.edu")
    }
    
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
                    value = mentorId,
                    onValueChange = { 
                        mentorId = it
                        mentorIdError = getMentorIdError(it)
                    },
                    label = { Text("Mentor ID") },
                    isError = mentorIdError != null,
                    supportingText = {
                        if (mentorIdError != null) {
                            Text(
                                text = mentorIdError!!,
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
                            "Please enter a valid Monash email (e.g., shoq0003@student.monash.edu or abcd1234@monash.edu)"
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
                        if (validateFullName(fullName) && validateMentorId(mentorId) && validateMonashEmail(email)) {
                            // Generate unique ID for the user
                            val userId = UUID.randomUUID().toString()
                            
                            // Create basic user document
                            val userDoc = hashMapOf(
                                "uid" to userId,
                                "name" to fullName,
                                "studentId" to mentorId,
                                "email" to email,
                                "isMentor" to true
                            )
                            
                            // Create basic mentor profile
                            val mentorDoc = hashMapOf(
                                "uid" to userId,
                                "name" to fullName,
                                "studentId" to mentorId,
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
                                                studentId = mentorId,
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
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = fullName.isNotEmpty() && 
                             mentorId.isNotEmpty() && 
                             email.isNotEmpty() && 
                             fullNameError == null &&
                             mentorIdError == null &&
                             emailError == null &&
                             mentorId.length == 8 &&
                             validateMonashEmail(email)
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