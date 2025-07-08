package com.example.ece452

import android.app.Application
import com.example.ece452.firebase.FirebaseConfig

class ClimbrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseConfig.initialize(this)
    }
} 