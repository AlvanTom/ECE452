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
} 