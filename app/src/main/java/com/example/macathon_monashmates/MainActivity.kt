package com.example.macathon_monashmates

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macathon_monashmates.ui.theme.MacathonMonashMatesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MacathonMonashMatesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WelcomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Gradient background colors
    val gradientColors = listOf(
        Color(0xFF6200EA), // Deep purple
        Color(0xFF03DAC5)  // Teal
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo at the top (increased size)
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "Monash Mates Logo",
                modifier = Modifier
                    .size(300.dp) // Increased from 200.dp to 300.dp
                    .padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Welcome text
            Text(
                text = "Welcome to Monash Mates!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Helping good students mentor others. Let’s get started.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Sign Up as Student button
            val studentSignUpInteractionSource = remember { MutableInteractionSource() }
            val isStudentSignUpPressed by studentSignUpInteractionSource.collectIsPressedAsState()
            val studentSignUpScale by animateFloatAsState(if (isStudentSignUpPressed) 0.95f else 1f)
            Button(
                onClick = {
                    Toast.makeText(context, "Sign Up as Student clicked!", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to student signup screen
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(studentSignUpScale)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EA)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                interactionSource = studentSignUpInteractionSource
            ) {
                Text(
                    text = "Sign Up as Student",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up as Mentor button
            val mentorSignUpInteractionSource = remember { MutableInteractionSource() }
            val isMentorSignUpPressed by mentorSignUpInteractionSource.collectIsPressedAsState()
            val mentorSignUpScale by animateFloatAsState(if (isMentorSignUpPressed) 0.95f else 1f)
            Button(
                onClick = {
                    Toast.makeText(context, "Sign Up as Mentor clicked!", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to mentor signup screen
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(mentorSignUpScale)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EA)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                interactionSource = mentorSignUpInteractionSource
            ) {
                Text(
                    text = "Sign Up as Mentor",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Log In button
            val logInInteractionSource = remember { MutableInteractionSource() }
            val isLogInPressed by logInInteractionSource.collectIsPressedAsState()
            val logInScale by animateFloatAsState(if (isLogInPressed) 0.95f else 1f)
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Log In clicked!", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to login screen
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(logInScale)
                    .clip(RoundedCornerShape(12.dp)),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                interactionSource = logInInteractionSource
            ) {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MacathonMonashMatesTheme {
        WelcomeScreen()
    }
}