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
    
    val context = LocalContext.current
    val userManager = remember { UserManager(context) }
    
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
            onValueChange = { studentId = it },
            label = { Text("Student ID") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Student ID",
                    tint = Color(0xFF003B5C)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color(0xFF003B5C)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Login Button
        Button(
            onClick = {
                val user = userManager.getUserByStudentId(studentId)
                if (user != null) {
                    userManager.setCurrentUser(user)
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                    
                    val activity = context as? ComponentActivity
                    if (activity?.intent?.getBooleanExtra("REDIRECT_TO_INTEREST", false) == true &&
                        activity.intent?.getStringExtra("STUDENT_ID") == studentId) {
                        // Coming from student signup - redirect to StudentInterestPage
                        val interestIntent = Intent(context, StudentInterestPage::class.java)
                        context.startActivity(interestIntent)
                    } else {
                        // Normal login flow
                        if (user.isMentor) {
                            // Mentor - redirect to MentorExpertisePage
                            val mentorIntent = Intent(context, MentorExpertisePage::class.java)
                            context.startActivity(mentorIntent)
                        } else {
                            // Student - redirect to HomePage
                            val intent = Intent(context, HomePage::class.java)
                            context.startActivity(intent)
                        }
                    }
                    (context as ComponentActivity).finish()
                } else {
                    Toast.makeText(context, "Invalid student ID!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = studentId.isNotEmpty()
        ) {
            Text("Login")
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