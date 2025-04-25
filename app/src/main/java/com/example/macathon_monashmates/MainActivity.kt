package com.example.macathon_monashmates

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macathon_monashmates.screens.SignUpPage
import com.example.macathon_monashmates.ui.theme.MacathonMonashMatesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MacathonMonashMatesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WelcomeScreenContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun WelcomeScreenContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gradientColors = listOf(
        Color(0xFF003B5C),  // Darker shade of Monash Blue for gradient
        Color(0xFF006DAE)   // Monash Blue
    )

    var signUpPressed by remember { mutableStateOf(false) }
    var logInPressed by remember { mutableStateOf(false) }
    val signUpScale by animateFloatAsState(if (signUpPressed) 0.95f else 1f)
    val logInScale by animateFloatAsState(if (logInPressed) 0.95f else 1f)

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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopLogoSection()
            MiddleContentSection(
                signUpScale = signUpScale,
                logInScale = logInScale,
                onSignUpClick = {
                    signUpPressed = true
                    val intent = Intent(context, SignUpPage::class.java)
                    context.startActivity(intent)
                    signUpPressed = false
                },
                onLogInClick = {
                    logInPressed = true
                    Toast.makeText(context, "Log In clicked!", Toast.LENGTH_SHORT).show()
                    logInPressed = false
                }
            )
            BottomLogoSection()
        }
    }
}

@Composable
private fun TopLogoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.monashmates),
            contentDescription = "MonashMates Logo",
            modifier = Modifier
                .size(350.dp)
                .padding(bottom = 20.dp)
        )
    }
}

@Composable
private fun MiddleContentSection(
    signUpScale: Float,
    logInScale: Float,
    onSignUpClick: () -> Unit,
    onLogInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            text = "Helping good students mentor others. Let's get started.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        SignUpButton(
            scale = signUpScale,
            onClick = onSignUpClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        LogInButton(
            scale = logInScale,
            onClick = onLogInClick
        )
    }
}

@Composable
private fun SignUpButton(scale: Float, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EA)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
    }
}

@Composable
private fun LogInButton(scale: Float, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp)),
        border = BorderStroke(1.dp, Color.White),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        )
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

@Composable
private fun BottomLogoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.monash_image_no_background),
            contentDescription = "Monash University Logo",
            modifier = Modifier
                .size(250.dp)
                .padding(top = 16.dp),
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MacathonMonashMatesTheme {
        WelcomeScreenContent()
    }
}