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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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

    fun clearError() {
        _errorMessage.value = null
    }
} 