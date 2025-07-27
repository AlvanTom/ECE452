package com.example.ece452.data

data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val userProfileImage: String? = null,
    val title: String,
    val location: String,
    val date: String,
    val timestamp: Long,
    val vScale: Int,
    val isIndoor: Boolean,
    val notes: String,
    val description: String,
    val mediaUrls: List<String> = emptyList(),
    val likes: Int = 0,
    val comments: Int = 0,
    val isLikedByCurrentUser: Boolean = false
)

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val userProfileImage: String? = null,
    val content: String,
    val timestamp: Long
)
