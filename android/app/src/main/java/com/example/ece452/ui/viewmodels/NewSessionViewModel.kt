package com.example.ece452.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ece452.firebase.FunctionsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NewSessionState {
    object Idle : NewSessionState()
    object Loading : NewSessionState()
    data class Success(val sessionId: String) : NewSessionState()
    data class Error(val message: String) : NewSessionState()
}

class NewSessionViewModel : ViewModel() {

    private val functionsService = FunctionsService()

    private val _createState = MutableStateFlow<NewSessionState>(NewSessionState.Idle)
    val createState = _createState.asStateFlow()

    fun createSession(title: String, gym: String, wallName: String) {
        viewModelScope.launch {
            _createState.value = NewSessionState.Loading
            try {
                // For now, we combine gym and wallName into the location field.
                // This can be changed later if the data model is updated.
                val location = if (gym.isNotBlank() && wallName.isNotBlank()) {
                    "$gym - $wallName"
                } else {
                    gym.ifBlank { wallName }
                }

                val result = functionsService.createSession(
                    title = title,
                    location = location,
                    gymName = gym.takeIf { it.isNotBlank() }
                )

                result.onSuccess { data ->
                    val sessionId = data?.get("sessionId") as? String
                    if (sessionId != null) {
                        _createState.value = NewSessionState.Success(sessionId)
                    } else {
                        _createState.value = NewSessionState.Error("Failed to get session ID.")
                    }
                }.onFailure {
                    _createState.value =
                        NewSessionState.Error(it.message ?: "An unknown error occurred.")
                }
            } catch (e: Exception) {
                _createState.value =
                    NewSessionState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    fun resetState() {
        _createState.value = NewSessionState.Idle
    }
} 