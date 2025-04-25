package com.example.macathon_monashmates

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase Auth
        FirebaseAuth.getInstance()
        
        // Initialize Firestore
        FirebaseFirestore.getInstance()
    }
} 