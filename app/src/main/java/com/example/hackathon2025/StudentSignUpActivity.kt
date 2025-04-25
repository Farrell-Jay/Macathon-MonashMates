package com.example.hackathon2025

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentSignUpActivity : AppCompatActivity() {
    private val monashCampuses = arrayOf(
        "Clayton",
        "Caulfield",
        "Peninsula",
        "Parkville",
        "Malaysia"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_signup)

        // Setup campus dropdown
        val campusAutoComplete = findViewById<AutoCompleteTextView>(R.id.campusAutoComplete)
        val campusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, monashCampuses)
        campusAutoComplete.setAdapter(campusAdapter)

        // Setup sign up button
        findViewById<Button>(R.id.signUpButton).setOnClickListener {
            validateAndSignUp()
        }
    }

    private fun validateAndSignUp() {
        val name = findViewById<TextInputEditText>(R.id.nameEditText).text.toString()
        val studentId = findViewById<TextInputEditText>(R.id.studentIdEditText).text.toString()
        val campus = findViewById<AutoCompleteTextView>(R.id.campusAutoComplete).text.toString()
        val units = findViewById<TextInputEditText>(R.id.unitsEditText).text.toString()

        if (validateInputs(name, studentId, campus, units)) {
            // TODO: Implement actual sign-up logic here
            // For now, just show a success message
            Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(name: String, studentId: String, campus: String, units: String): Boolean {
        var isValid = true

        if (name.isBlank()) {
            findViewById<TextInputLayout>(R.id.nameInputLayout).error = "Name is required"
            isValid = false
        } else {
            findViewById<TextInputLayout>(R.id.nameInputLayout).error = null
        }

        if (studentId.isBlank()) {
            findViewById<TextInputLayout>(R.id.studentIdInputLayout).error = "Student ID is required"
            isValid = false
        } else {
            findViewById<TextInputLayout>(R.id.studentIdInputLayout).error = null
        }

        if (campus.isBlank()) {
            findViewById<TextInputLayout>(R.id.campusInputLayout).error = "Campus is required"
            isValid = false
        } else {
            findViewById<TextInputLayout>(R.id.campusInputLayout).error = null
        }

        if (units.isBlank()) {
            findViewById<TextInputLayout>(R.id.unitsInputLayout).error = "At least one unit is required"
            isValid = false
        } else {
            findViewById<TextInputLayout>(R.id.unitsInputLayout).error = null
        }

        return isValid
    }

    private suspend fun validateUnits(units: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implement actual unit validation using Monash API
                // This is a placeholder for the actual API call
                true
            } catch (e: Exception) {
                false
            }
        }
    }
} 