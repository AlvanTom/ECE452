package com.example.ece452.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ece452.firebase.AuthService
import com.example.ece452.firebase.FunctionsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignupViewModel : ViewModel() {

    private val authService = AuthService()
    private val functionsService = FunctionsService()

    private val _signupState = MutableStateFlow<SignupState>(SignupState.Idle)
    val signupState = _signupState.asStateFlow()

    fun signup(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading
            try {
                val result = authService.signup(email, password)
                result.onSuccess { authResult ->
                    // Successfully created user in Auth
                    val user = authResult.user
                    if (user != null) {
                        // Now create the user profile in Firestore
                        val profileResult = functionsService.createUser(displayName)
                        profileResult.onSuccess {
                            _signupState.value = SignupState.Success
                        }.onFailure {
                            _signupState.value = SignupState.Error(it.message ?: "Failed to create user profile.")
                        }
                    } else {
                        _signupState.value = SignupState.Error("Signup succeeded but user was not found.")
                    }
                }.onFailure {
                    // Failed to create user in Auth
                    _signupState.value = SignupState.Error(it.message ?: "Failed to sign up.")
                }
            } catch (e: Exception) {
                _signupState.value = SignupState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }
}

sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    object Success : SignupState()
    data class Error(val message: String) : SignupState()
} 