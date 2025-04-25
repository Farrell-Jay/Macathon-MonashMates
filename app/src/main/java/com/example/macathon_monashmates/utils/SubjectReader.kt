package com.example.macathon_monashmates.utils

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class Subject(
    val unitId: String,
    val unitName: String
) {
    override fun toString(): String = "$unitId - $unitName"
}

object SubjectReader {
    suspend fun readSubjects(context: Context): List<Subject> = withContext(Dispatchers.IO) {
        val subjects = mutableListOf<Subject>()
        try {
            val inputStream = context.assets.open("subjects.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip header row
            reader.readLine()
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line!!.split(",")
                if (parts.size >= 2) {
                    subjects.add(Subject(parts[0].trim(), parts[1].trim()))
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        subjects
    }
} 