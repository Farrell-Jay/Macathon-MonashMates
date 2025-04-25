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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color

data class TimeSlot(
    val dayOfWeek: String,
    val startTime: LocalTime,
    val endTime: LocalTime
)

class MentorExpertisePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentorExpertiseScreen()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MentorExpertiseScreen() {
    val subjects = remember { mutableStateListOf<Subject>() }
    var selectedSubjects by remember { mutableStateOf(setOf<Subject>()) }
    var selectedExpertiseAreas by remember { mutableStateOf(setOf<String>()) }
    var showSubjectDialog by remember { mutableStateOf(false) }
    var showExpertiseDialog by remember { mutableStateOf(false) }
    var expertiseLevels by remember { mutableStateOf(mapOf<String, String>()) }
    var bio by remember { mutableStateOf("") }
    var timeSlots by remember { mutableStateOf(listOf<TimeSlot>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("Monday") }
    var selectedStartTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var selectedEndTime by remember { mutableStateOf(LocalTime.of(17, 0)) }
    
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val expertiseAreas = listOf(
        "Cybersecurity",
        "Data Science",
        "Artificial Intelligence",
        "Software Engineering",
        "Cloud Computing",
        "Machine Learning",
        "Web Development",
        "Mobile Development",
        "Database Management",
        "Network Engineering"
    )
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userManager = remember { UserManager(context) }
    val currentUser = userManager.getCurrentUser()
    
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
        // Back button with Monash styling
        IconButton(
            onClick = { 
                val intent = Intent(context, MentorSignupPage::class.java)
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
                            text = "Please complete your mentor profile to help students find you.",
                            fontSize = 16.sp,
                            color = MonashDarkBlue
                        )
                    }
                }
            }
            
            // Bio Section
            item {
                Text(
                    text = "Mentor Profile Setup",
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
                    placeholder = { Text("Tell students about yourself and your teaching style") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonashBlue,
                        unfocusedBorderColor = MonashBlue.copy(alpha = 0.5f),
                        focusedLabelColor = MonashBlue,
                        unfocusedLabelColor = MonashBlue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            
            // Areas of Expertise Section
            item {
                Text(
                    text = "Areas of Expertise",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedButton(
                    onClick = { showExpertiseDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MonashBlue
                    ),
                    border = BorderStroke(1.dp, MonashBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Areas of Expertise")
                }
            }
            
            // Selected Expertise Areas
            if (selectedExpertiseAreas.isNotEmpty()) {
                items(selectedExpertiseAreas.toList()) { expertise ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MonashLightBlue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = expertise,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonashBlue
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Expertise Level Dropdown
                            var expanded by remember { mutableStateOf(false) }
                            val expertiseOptions = listOf("Beginner", "Intermediate", "Advanced", "Expert")
                            var selectedLevel by remember { mutableStateOf(expertiseLevels[expertise] ?: "Select Level") }
                            
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedLevel,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Expertise Level") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MonashBlue,
                                            unfocusedBorderColor = MonashBlue.copy(alpha = 0.5f),
                                            focusedLabelColor = MonashBlue,
                                            unfocusedLabelColor = MonashBlue.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        expertiseOptions.forEach { level ->
                                            DropdownMenuItem(
                                                text = { Text(level) },
                                                onClick = {
                                                    selectedLevel = level
                                                    expertiseLevels = expertiseLevels + (expertise to level)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Units Taken Section
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
            
            // Availability Section
            item {
                Text(
                    text = "Weekly Availability",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                // Day Selection
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDay,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Day") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MonashBlue,
                                unfocusedBorderColor = MonashBlue.copy(alpha = 0.5f),
                                focusedLabelColor = MonashBlue,
                                unfocusedLabelColor = MonashBlue.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            daysOfWeek.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        selectedDay = day
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MonashBlue
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Time Slot",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Add Time Slot")
                }
            }
            
            // Time Slots
            items(timeSlots) { slot ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MonashLightBlue
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = slot.dayOfWeek,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonashBlue
                            )
                            Text(
                                text = "${slot.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${slot.endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                        IconButton(
                            onClick = {
                                timeSlots = timeSlots.filter { it != slot }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Time Slot",
                                tint = MonashBlue
                            )
                        }
                    }
                }
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
                            
                            // Debug logging
                            println("Debug - Saving expertise areas: $selectedExpertiseAreas")
                            println("Debug - Saving expertise levels: $expertiseLevels")
                            println("Debug - Saving subjects: $selectedSubjects")
                            
                            editor.putString("${currentUser.studentId}_bio", bio)
                            
                            // Save time slots
                            val timeSlotString = timeSlots.joinToString("|||") { slot -> 
                                "${slot.dayOfWeek}|||${slot.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))}|||${slot.endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}" 
                            }
                            editor.putString("${currentUser.studentId}_timeSlots", timeSlotString)
                            
                            // Save expertise areas and levels separately
                            editor.putString("${currentUser.studentId}_expertiseAreas", selectedExpertiseAreas.joinToString("|||"))
                            editor.putString("${currentUser.studentId}_expertiseLevels", expertiseLevels.entries.joinToString("|||") { "${it.key}::${it.value}" })
                            
                            // Save subjects
                            editor.putString("${currentUser.studentId}_subjects", selectedSubjects.joinToString("|||") { it.toString() })
                            
                            editor.apply()
                            
                            // Debug - Verify saved data
                            println("Debug - Verified saved expertise areas: ${sharedPreferences.getString("${currentUser.studentId}_expertiseAreas", null)}")
                            println("Debug - Verified saved expertise levels: ${sharedPreferences.getString("${currentUser.studentId}_expertiseLevels", null)}")
                            println("Debug - Verified saved subjects: ${sharedPreferences.getString("${currentUser.studentId}_subjects", null)}")
                            
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
                    enabled = selectedSubjects.isNotEmpty() && bio.isNotEmpty() && timeSlots.isNotEmpty()
                ) {
                    Text("Complete Profile")
                }
            }
        }
    }
    
    if (showTimePicker) {
        var isSelectingStartTime by remember { mutableStateOf(true) }
        var tempStartTime by remember { mutableStateOf(selectedStartTime) }
        var tempEndTime by remember { mutableStateOf(selectedEndTime) }
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { 
                Text(
                    text = if (isSelectingStartTime) "Select Start Time" else "Select End Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonashBlue
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSelectingStartTime) {
                        val startTimeState = rememberTimePickerState(
                            initialHour = tempStartTime.hour,
                            initialMinute = tempStartTime.minute
                        )
                        TimePicker(
                            state = startTimeState,
                            layoutType = TimePickerLayoutType.Vertical,
                            colors = TimePickerDefaults.colors(
                                containerColor = MonashLightBlue,
                                clockDialColor = MonashBlue,
                                clockDialSelectedContentColor = Color.White,
                                selectorColor = MonashBlue
                            )
                        )
                        
                        // Update tempStartTime whenever the time state changes
                        DisposableEffect(startTimeState.hour, startTimeState.minute) {
                            tempStartTime = LocalTime.of(startTimeState.hour, startTimeState.minute)
                            onDispose { }
                        }
                    } else {
                        val endTimeState = rememberTimePickerState(
                            initialHour = tempEndTime.hour,
                            initialMinute = tempEndTime.minute
                        )
                        TimePicker(
                            state = endTimeState,
                            layoutType = TimePickerLayoutType.Vertical,
                            colors = TimePickerDefaults.colors(
                                containerColor = MonashLightBlue,
                                clockDialColor = MonashBlue,
                                clockDialSelectedContentColor = Color.White,
                                selectorColor = MonashBlue
                            )
                        )
                        
                        // Update tempEndTime whenever the time state changes
                        DisposableEffect(endTimeState.hour, endTimeState.minute) {
                            tempEndTime = LocalTime.of(endTimeState.hour, endTimeState.minute)
                            onDispose { }
                        }
                    }
                    
                    if (!isSelectingStartTime) {
                        Text(
                            text = "Selected start time: ${tempStartTime.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                            color = MonashBlue,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Debug current selection
                    LaunchedEffect(tempStartTime, tempEndTime) {
                        println("Debug - Current tempStartTime: ${tempStartTime.format(DateTimeFormatter.ofPattern("h:mm a"))}")
                        println("Debug - Current tempEndTime: ${tempEndTime.format(DateTimeFormatter.ofPattern("h:mm a"))}")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isSelectingStartTime) {
                            println("Debug - Confirming start time: ${tempStartTime.format(DateTimeFormatter.ofPattern("h:mm a"))}")
                            isSelectingStartTime = false
                        } else {
                            println("Debug - Confirming end time: ${tempEndTime.format(DateTimeFormatter.ofPattern("h:mm a"))}")
                            if (tempEndTime.isAfter(tempStartTime)) {
                                // Update the actual selected times
                                selectedStartTime = tempStartTime
                                selectedEndTime = tempEndTime
                                
                                // Create and add the new time slot
                                val newTimeSlot = TimeSlot(
                                    dayOfWeek = selectedDay,
                                    startTime = selectedStartTime,
                                    endTime = selectedEndTime
                                )
                                
                                println("Debug - Adding new time slot: $selectedDay ${selectedStartTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${selectedEndTime.format(DateTimeFormatter.ofPattern("h:mm a"))}")
                                
                                timeSlots = timeSlots + newTimeSlot
                                showTimePicker = false
                                
                                // Show confirmation toast
                                Toast.makeText(
                                    context,
                                    "Time slot added: ${selectedStartTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${selectedEndTime.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "End time must be after start time",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Text(
                        text = if (isSelectingStartTime) "Next" else "Add",
                        color = MonashBlue
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        if (!isSelectingStartTime) {
                            isSelectingStartTime = true
                        } else {
                            showTimePicker = false
                        }
                    }
                ) {
                    Text(
                        text = if (isSelectingStartTime) "Cancel" else "Back",
                        color = MonashBlue
                    )
                }
            }
        )
    }
    
    if (showExpertiseDialog) {
        AlertDialog(
            onDismissRequest = { showExpertiseDialog = false },
            title = { Text("Select Areas of Expertise") },
            text = {
                Column {
                    expertiseAreas.forEach { area ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = area in selectedExpertiseAreas,
                                onCheckedChange = { checked ->
                                    selectedExpertiseAreas = if (checked) {
                                        selectedExpertiseAreas + area
                                    } else {
                                        selectedExpertiseAreas - area
                                    }
                                }
                            )
                            Text(area)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExpertiseDialog = false }) {
                    Text("Done")
                }
            }
        )
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