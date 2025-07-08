package com.example.ece452.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.ece452.data.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

class PostViewModel : ViewModel() {

    // Current guess on implementation for creating a post
    // No media upload currently

    private val _activePost = MutableStateFlow<Post?>(null)
    val activePost: StateFlow<Post?> = _activePost.asStateFlow()

    private val _postHistory = MutableStateFlow<List<Post>>(emptyList())
    val postHistory: StateFlow<List<Post>> = _postHistory.asStateFlow()

    fun createPost(
        title: String,
        location: String,
        date: String,
        vScale: Int,
        isIndoor: Boolean,
        notes: String
    ) {
        val newPost = Post (
            id = UUID.randomUUID().toString(),
            title = title,
            location = location,
            date = date,
            vScale = vScale,
            isIndoor = isIndoor,
            notes = notes
        )
        _activePost.value = newPost
    }

    fun saveActivePost() {
        _activePost.value?.let { post ->
            _postHistory.update { listOf(post) + it } // newest first
        }
        _activePost.value = null
    }
}
