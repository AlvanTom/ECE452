package com.example.ece452.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.ece452.data.Attempt
import com.example.ece452.data.Route
import com.example.ece452.data.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class SessionViewModel : ViewModel() {

    private val _sessionHistory = MutableStateFlow<List<Session>>(emptyList())
    val sessionHistory: StateFlow<List<Session>> = _sessionHistory.asStateFlow()

    private val _activeSession = MutableStateFlow<Session?>(null)
    val activeSession: StateFlow<Session?> = _activeSession.asStateFlow()

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
    }

    fun addRouteToActiveSession(route: Route) {
        _activeSession.update { currentSession ->
            currentSession?.copy(
                routes = currentSession.routes + route,
                routesCount = currentSession.routes.size + 1
            )
        }
    }

    fun endActiveSession() {
        activeSession.value?.let { session ->
            _sessionHistory.update { history ->
                // Add to the beginning of the list to show newest first
                listOf(session) + history
            }
        }
        _activeSession.value = null
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

} 