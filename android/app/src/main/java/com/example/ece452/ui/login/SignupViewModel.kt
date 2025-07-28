package com.example.ece452.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ece452.firebase.AuthService
import com.example.ece452.firebase.FunctionsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.UserProfileChangeRequest

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

                // Update the user's display name in Firebase Auth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                
                try {
                    user.updateProfile(profileUpdates).await()
                    
                    // Create user profile in database
                    val result = functionsService.createUser(displayName)
                    if (result.isSuccess) {
                        _signupState.value = SignupState.Success
                    } else {
                        _signupState.value = SignupState.Error("Failed to create user profile.")
                    }
                } catch (e: Exception) {
                    _signupState.value = SignupState.Error("Failed to update user profile: ${e.message}")
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