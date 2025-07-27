package com.example.ece452.firebase

import com.google.firebase.functions.FirebaseFunctions
import com.example.ece452.data.Session
import com.example.ece452.data.Route
import com.example.ece452.data.Attempt
import kotlinx.coroutines.tasks.await

data class SessionCreationResult(
    val sessionId: String,
    val routeIds: List<String>
)

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

    suspend fun createSession(session: Session): Result<SessionCreationResult> {
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
                },
                "mediaUrl" to route.mediaUri // Include media URL (will be local URI for new routes)
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
                val routeIds = responseData?.get("routeIds") as? List<*>
                if (sessionId != null && routeIds != null) {
                    val routeIdStrings = routeIds.filterIsInstance<String>()
                    Result.success(SessionCreationResult(sessionId, routeIdStrings))
                } else {
                    Result.failure(Exception("Invalid response: missing sessionId or routeIds"))
                }
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    suspend fun getUserSessions(userId: String): Result<List<String>> {
        val data = mapOf("uid" to userId)
        
        return callFunction("getSessionsByUID", data).fold(
            onSuccess = { responseData ->
                val sessionIds = responseData?.get("sessionIds") as? List<String>
                if (sessionIds != null) {
                    Result.success(sessionIds)
                } else {
                    Result.failure(Exception("Invalid response: missing sessionIds"))
                }
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    suspend fun getSessionById(sessionId: String): Result<Session> {
        val data = mapOf("sessionId" to sessionId)
        
        return callFunction("getSessionByID", data).fold(
            onSuccess = { responseData ->
                try {
                    val sessionData = responseData?.get("sessionData") as? Map<String, Any>
                    val routesData = responseData?.get("routesData") as? List<Map<String, Any>>
                    
                    if (sessionData != null && routesData != null) {
                        val routes = routesData.map { routeMap ->
                            val attempts = (routeMap["attempts"] as? List<Map<String, Any>>)?.map { attemptMap ->
                                Attempt(
                                    id = attemptMap["id"] as? String ?: "",
                                    success = attemptMap["success"] as? Boolean ?: false,
                                    createdAt = attemptMap["createdAt"] as? String ?: ""
                                )
                            } ?: emptyList()
                            
                            Route(
                                id = routeMap["id"] as? String ?: "",
                                routeName = routeMap["routeName"] as? String ?: "",
                                difficulty = routeMap["difficulty"] as? String ?: "",
                                tags = (routeMap["tags"] as? List<String>) ?: emptyList(),
                                notes = routeMap["notes"] as? String,
                                attempts = attempts,
                                mediaUri = routeMap["mediaUrl"] as? String  // Map backend mediaUrl to frontend mediaUri
                            )
                        }
                        
                        val session = Session(
                            id = responseData["sessionId"] as? String ?: "",
                            userId = sessionData["userId"] as? String ?: "",
                            title = sessionData["title"] as? String ?: "",
                            location = sessionData["location"] as? String ?: "",
                            isIndoor = sessionData["isIndoor"] as? Boolean ?: true,
                            gymName = sessionData["gymName"] as? String,
                            createdAt = sessionData["createdAt"] as? String ?: "",
                            routesCount = routes.size,
                            routes = routes
                        )
                        
                        Result.success(session)
                    } else {
                        Result.failure(Exception("Invalid response: missing session or routes data"))
                    }
                } catch (e: Exception) {
                    Result.failure(Exception("Failed to parse session data: ${e.message}"))
                }
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    suspend fun updateSession(session: Session): Result<SessionCreationResult> {
        println("[DEBUG] updateSession called with session: $session")
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
                },
                "mediaUrl" to route.mediaUri // Include existing media URL to preserve it
            )
        }

        val data = buildMap<String, Any> {
            put("sessionId", session.id)
            put("title", session.title)
            session.gymName?.let { put("gymName", it) }
            put("routes", backendRoutes)
        }

        return callFunction("putSession", data).fold(
            onSuccess = { responseData ->
                val sessionId = responseData?.get("sessionId") as? String
                val routeIds = responseData?.get("routeIds") as? List<*>
                if (sessionId != null && routeIds != null) {
                    val routeIdStrings = routeIds.filterIsInstance<String>()
                    Result.success(SessionCreationResult(sessionId, routeIdStrings))
                } else {
                    Result.failure(Exception("Invalid response: missing sessionId or routeIds"))
                }
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    suspend fun updateRouteMedia(sessionId: String, routeId: String, mediaUrl: String): Result<Boolean> {
        val data = mapOf(
            "sessionId" to sessionId,
            "routeId" to routeId,
            "mediaUrl" to mediaUrl
        )

        return callFunction("updateRouteMedia", data).fold(
            onSuccess = { responseData ->
                val success = responseData?.get("success") as? Boolean
                Result.success(success ?: false)
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }
} 