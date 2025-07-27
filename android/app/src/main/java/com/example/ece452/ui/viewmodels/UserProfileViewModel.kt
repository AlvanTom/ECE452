package com.example.ece452.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val _profilePhotoUrl = MutableStateFlow<String?>(null)
    val profilePhotoUrl: StateFlow<String?> = _profilePhotoUrl.asStateFlow()
    
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        setupAuthStateListener()
        loadCurrentUserProfile()
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // User signed in, load their profile photo
                _profilePhotoUrl.value = currentUser.photoUrl?.toString()
            } else {
                // User signed out, clear profile photo
                _profilePhotoUrl.value = null
            }
        }
        Firebase.auth.addAuthStateListener(authStateListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the auth state listener to prevent memory leaks
        authStateListener?.let { listener ->
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    private fun loadCurrentUserProfile() {
        val currentUser = Firebase.auth.currentUser
        _profilePhotoUrl.value = currentUser?.photoUrl?.toString()
    }

    fun refreshProfilePhoto() {
        loadCurrentUserProfile()
    }

    fun updateProfilePhoto(photoUrl: String) {
        _profilePhotoUrl.value = photoUrl
    }

    fun clearProfilePhoto() {
        _profilePhotoUrl.value = null
    }

    fun loadProfilePhotoForUser(userId: String?) {
        if (userId == null) {
            _profilePhotoUrl.value = null
            return
        }
        
        val currentUser = Firebase.auth.currentUser
        // Only load if the current user matches the requested userId
        if (currentUser?.uid == userId) {
            _profilePhotoUrl.value = currentUser.photoUrl?.toString()
        } else {
            _profilePhotoUrl.value = null
        }
    }
} 