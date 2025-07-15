package com.example.ece452.data

data class Post(
    val id: String,
    val title: String,
    val location: String,
    val date: String,
    val vScale: Int,
    val isIndoor: Boolean,
    val notes: String,
    val mediaUrls: List<String> = emptyList() // Added for media upload
)
