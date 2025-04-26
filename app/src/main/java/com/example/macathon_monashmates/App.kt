package com.example.macathon_monashmates

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.macathon_monashmates.managers.UserManager

class App : Application() {
    
    companion object {
        private const val TAG = "MonashMatesApp"
    }
    
    private lateinit var userManager: UserManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application starting up")
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase Auth
        FirebaseAuth.getInstance()
        
        // Initialize Firestore
        FirebaseFirestore.getInstance()
        
        // Initialize UserManager
        userManager = UserManager(this)
        
        // Sync Firebase authentication with current user
        userManager.syncFirebaseAuth()
        
        Log.d(TAG, "Firebase authentication sync attempted")
    }
} 