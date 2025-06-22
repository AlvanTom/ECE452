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
            val user = authService.signUp(email, password)
            if (user != null) {
                // Force refresh the token to solve race condition
                user.getIdToken(true).await()

                val result = functionsService.createUser(displayName)
                if (result.isSuccess) {
                    _signupState.value = SignupState.Success
                } else {
                    _signupState.value = SignupState.Error("Failed to create user profile.")
                }
            } else {
                _signupState.value = SignupState.Error("Failed to sign up.")
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