package com.example.ece452.firebase

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthService {

    private val auth: FirebaseAuth = FirebaseConfig.auth

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun signup(email: String, pass: String): Result<AuthResult> {
        return try {
            val res = auth.createUserWithEmailAndPassword(email, pass).await()
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            // Handle exceptions
            null
        }
    }
} 