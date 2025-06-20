package com.example.ece452.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.functions.FirebaseFunctions

object FirebaseConfig {
    
    fun initialize(app: android.app.Application) {
        // Initialize Firebase
        FirebaseApp.initializeApp(app)
    }
    
    // Firebase Functions instance
    val functions: FirebaseFunctions by lazy {
        FirebaseFunctions.getInstance()
    }
} 