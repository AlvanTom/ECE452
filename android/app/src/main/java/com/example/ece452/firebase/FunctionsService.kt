package com.example.ece452.firebase

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FunctionsService {
    
    private val functions: FirebaseFunctions = FirebaseConfig.functions
    
    suspend fun callFunction(functionName: String, data: Map<String, Any>): Result<Map<String, Any>?> {
        return try {
            val result = functions.getHttpsCallable(functionName).call(data).await()
            val resultData = if (result.data is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                result.data as Map<String, Any>
            } else {
                null
            }
            Result.success(resultData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun callFunction(functionName: String): Result<Map<String, Any>?> {
        return try {
            val result = functions.getHttpsCallable(functionName).call().await()
            val resultData = if (result.data is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                result.data as Map<String, Any>
            } else {
                null
            }
            Result.success(resultData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(displayName: String): Result<Map<String, Any>?> {
        val data = mapOf(
            "displayName" to displayName
        )
        return callFunction("createUser", data)
    }
    
    suspend fun getUserSessions(uid: String): Result<Map<String, Any>?> {
        val data = mapOf(
            "uid" to uid
        )
        return callFunction("getUserSessions", data)
    }
    
    suspend fun getActiveSessions(uid: String): Result<Map<String, Any>?> {
        val data = mapOf(
            "uid" to uid
        )
        return callFunction("getActiveSessions", data)
    }
    
    suspend fun getSessionByID(sessionId: String): Result<Map<String, Any>?> {
        val data = mapOf(
            "sessionId" to sessionId
        )
        return callFunction("getSessionByID", data)
    }

    suspend fun createSession(title: String, location: String, gymName: String?): Result<Map<String, Any>?> {
        val uid = FirebaseConfig.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        val sessionData = mutableMapOf<String, Any>(
            "uid" to uid,
            "title" to title,
            "location" to location,
            "isIndoor" to true, // Defaulting to indoor
            "routes" to emptyList<Map<String, Any>>() // Start with no routes
        )

        if (gymName != null && gymName.isNotBlank()) {
            sessionData["gymName"] = gymName
        }

        return callFunction("createSession", sessionData)
    }
} 