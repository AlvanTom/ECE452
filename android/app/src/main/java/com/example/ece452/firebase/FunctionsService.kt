package com.example.ece452.firebase

import com.google.firebase.functions.FirebaseFunctions
import com.example.ece452.data.Session
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

    suspend fun createSession(session: Session): Result<String> {
        val backendRoutes = session.routes.map { route ->
            mapOf(
                "routeName" to route.routeName,
                "difficulty" to route.difficulty,
                "tags" to route.tags,
                "notes" to (route.notes ?: ""),
                "attempts" to route.attempts.map { attempt ->
                    mapOf(
                        "success" to attempt.success,
                        "createdAt" to attempt.createdAt
                    )
                }
            )
        }

        val data = buildMap<String, Any> {
            put("uid", session.userId)
            put("title", session.title)
            put("location", session.location)
            put("isIndoor", session.isIndoor)
            session.gymName?.let { put("gymName", it) }
            put("routes", backendRoutes)
        }

        return callFunction("createSession", data).fold(
            onSuccess = { responseData ->
                val sessionId = responseData?.get("sessionId") as? String
                if (sessionId != null) {
                    Result.success(sessionId)
                } else {
                    Result.failure(Exception("Invalid response: missing sessionId"))
                }
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }
} 