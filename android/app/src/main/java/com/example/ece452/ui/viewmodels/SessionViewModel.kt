package com.example.ece452.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ece452.data.Attempt
import com.example.ece452.data.Route
import com.example.ece452.data.Session
import com.example.ece452.firebase.AuthService
import com.example.ece452.firebase.FunctionsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SessionViewModel : ViewModel() {

    private val functionsService = FunctionsService()
    private val authService = AuthService()

    private val _sessionHistory = MutableStateFlow<List<Session>>(emptyList())
    val sessionHistory: StateFlow<List<Session>> = _sessionHistory.asStateFlow()

    private val _activeSession = MutableStateFlow<Session?>(null)
    val activeSession: StateFlow<Session?> = _activeSession.asStateFlow()

    private val _isEndingSession = MutableStateFlow(false)
    val isEndingSession: StateFlow<Boolean> = _isEndingSession.asStateFlow()

    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Load session history when ViewModel is created
        loadSessionHistory()
    }

    fun createSession(title: String, gym: String, wallName: String) {
        val location = if (gym.isNotBlank() && wallName.isNotBlank()) {
            "$gym - $wallName"
        } else {
            gym.ifBlank { wallName }
        }

        val newSession = Session(
            id = UUID.randomUUID().toString(),
            userId = "local_user", // Placeholder for local development
            title = title,
            location = location,
            isIndoor = true,
            gymName = gym.takeIf { it.isNotBlank() },
            createdAt = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date()),
            routes = emptyList()
        )
        _activeSession.value = newSession
        clearError()
    }

    fun addRouteToActiveSession(route: Route) {
        _activeSession.update { currentSession ->
            currentSession?.copy(
                routes = currentSession.routes + route,
                routesCount = currentSession.routes.size + 1
            )
        }
    }

    suspend fun endActiveSession(): Boolean {
        val session = activeSession.value ?: return false
        val currentUserId = authService.getCurrentUserId() ?: return false

        _isEndingSession.value = true
        clearError()
        
        return try {
            // Update session with real user ID before sending to backend
            val sessionWithUserId = session.copy(userId = currentUserId)
            
            val result = functionsService.createSession(sessionWithUserId)
            
            result.fold(
                onSuccess = { sessionId ->
                    // Only clear session on successful save
                    _activeSession.value = null
                    // Refresh session history to include the new session
                    refreshSessionHistory()
                    true
                },
                onFailure = { exception ->
                    // Keep session and show error - don't clear it
                    _errorMessage.value = "Failed to save session: ${exception.message ?: "Unknown error"}"
                    false
                }
            )
        } catch (e: Exception) {
            // Keep session and show error - don't clear it
            _errorMessage.value = "Failed to save session: ${e.message ?: "Unknown error"}"
            false
        } finally {
            _isEndingSession.value = false
        }
    }

    fun addAttemptToRoute(routeId: String, isSuccess: Boolean) {
        _activeSession.update { currentSession ->
            currentSession?.let { session ->
                val updatedRoutes = session.routes.map { route ->
                    if (route.id == routeId) {
                        val newAttempt = Attempt(
                            id = UUID.randomUUID().toString(),
                            success = isSuccess,
                            createdAt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                        route.copy(attempts = route.attempts + newAttempt)
                    } else {
                        route
                    }
                }
                session.copy(routes = updatedRoutes)
            }
        }
    }

    fun loadSessionHistory() {
        val currentUserId = authService.getCurrentUserId()
        if (currentUserId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoadingHistory.value = true
            clearError()
            
            try {
                // First, get all session IDs for the user
                val sessionIdsResult = functionsService.getUserSessions(currentUserId)
                
                sessionIdsResult.fold(
                    onSuccess = { sessionIds ->
                        if (sessionIds.isEmpty()) {
                            _sessionHistory.value = emptyList()
                        } else {
                            // For each session ID, get the full session details
                            val sessions = mutableListOf<Session>()
                            
                            for (sessionId in sessionIds) {
                                val sessionResult = functionsService.getSessionById(sessionId)
                                sessionResult.fold(
                                    onSuccess = { session ->
                                        sessions.add(session)
                                    },
                                    onFailure = { exception ->
                                        // Log error but continue with other sessions
                                        println("Failed to load session $sessionId: ${exception.message}")
                                    }
                                )
                            }
                            
                            // Sort sessions by creation date (newest first)
                            _sessionHistory.value = sessions.sortedByDescending { it.createdAt }
                        }
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Failed to load session history: ${exception.message ?: "Unknown error"}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load session history: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }

    fun refreshSessionHistory() {
        loadSessionHistory()
    }

    fun clearError() {
        _errorMessage.value = null
    }
} 