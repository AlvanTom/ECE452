package com.example.ece452.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ece452.data.Post
import com.example.ece452.data.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.*
import kotlin.random.Random
import android.net.Uri
import com.example.ece452.firebase.FirebaseConfig
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.StorageMetadata

class PostViewModel : ViewModel() {

    private val _activePost = MutableStateFlow<Post?>(null)
    val activePost: StateFlow<Post?> = _activePost.asStateFlow()

    private val _feedPosts = MutableStateFlow<List<Post>>(emptyList())
    val feedPosts: StateFlow<List<Post>> = _feedPosts.asStateFlow()

    private val _personalPosts = MutableStateFlow<List<Post>>(emptyList())
    val personalPosts: StateFlow<List<Post>> = _personalPosts.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMorePosts = MutableStateFlow(true)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()

    private var currentPage = 0
    private val postsPerPage = 10
    private val allMockPosts = generateMockPosts()

    init {
        loadMorePosts()
    }

    suspend fun uploadMediaFiles(uris: List<Uri>, userId: String): List<String> {
        val storage = FirebaseConfig.storage
        val urls = mutableListOf<String>()
        for (uri in uris) {
            val fileName = "${userId}_${System.currentTimeMillis()}_${uri.lastPathSegment}"
            val ref: StorageReference = storage.reference.child("post_media/$fileName")
            
            // Set metadata with userId for security rules
            val metadata = StorageMetadata.Builder()
                .setCustomMetadata("userId", userId)
                .build()
            
            val uploadTask = ref.putFile(uri, metadata).await()
            val url = ref.downloadUrl.await().toString()
            urls.add(url)
        }
        return urls
    }

    suspend fun createPost(post: Post): Result<String> {
        val data = buildMap<String, Any> {
            put("uid", post.userId)
            put("username", post.username)
            post.userProfileImage?.let { put("userProfileImage", it) }
            put("title", post.title)
            put("location", post.location)
            put("date", post.date)
            put("vScale", post.vScale)
            put("isIndoor", post.isIndoor)
            put("notes", post.notes)
            put("description", post.description)
            put("mediaUrls", post.mediaUrls)
        }
    
        return try {
            val result = FirebaseConfig.functions
                .getHttpsCallable("createPost")
                .call(data)
                .await()
    
            val postId = (result.data as? Map<*, *>)?.get("postId") as? String
            if (postId != null) {
                Result.success(postId)
            } else {
                Result.failure(Exception("Missing postId in response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    // fun createPost(
    //     title: String,
    //     location: String,
    //     date: String,
    //     vScale: Int,
    //     isIndoor: Boolean,
    //     notes: String,
    //     description: String = notes,
    //     mediaUrls: List<String> = emptyList()
    // ) {
    //     val newPost = Post(
    //         id = UUID.randomUUID().toString(),
    //         userId = "current_user", // TODO: Get from auth
    //         username = "Me", // TODO: Get from auth
    //         title = title,
    //         location = location,
    //         date = date,
    //         timestamp = System.currentTimeMillis(),
    //         vScale = vScale,
    //         isIndoor = isIndoor,
    //         notes = notes,
    //         description = description,
    //         mediaUrls = mediaUrls
    //     )
    //     _activePost.value = newPost
    // }

    fun saveActivePost() {
        _activePost.value?.let { post ->
            _personalPosts.update { listOf(post) + it } // Add to personal posts
            _feedPosts.update { listOf(post) + it } // Add to feed (newest first)
        }
        _activePost.value = null
    }

    fun loadMorePosts() {
        if (_isLoading.value || !_hasMorePosts.value) return
        
        _isLoading.value = true
        
        viewModelScope.launch {
            delay(500) // Simulate loading time
            
            val startIndex = currentPage * postsPerPage
            val endIndex = minOf(startIndex + postsPerPage, allMockPosts.size)
            
            if (startIndex < allMockPosts.size) {
                val newPosts = allMockPosts.subList(startIndex, endIndex)
                _feedPosts.update { currentPosts ->
                    currentPosts + newPosts
                }
                currentPage++
            }
            
            _hasMorePosts.value = endIndex < allMockPosts.size
            _isLoading.value = false
        }
    }

    fun toggleLike(postId: String) {
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    post.copy(
                        likes = if (post.isLikedByCurrentUser) post.likes - 1 else post.likes + 1,
                        isLikedByCurrentUser = !post.isLikedByCurrentUser
                    )
                } else {
                    post
                }
            }
        }
        
        // Also update personal posts if it exists there
        _personalPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    post.copy(
                        likes = if (post.isLikedByCurrentUser) post.likes - 1 else post.likes + 1,
                        isLikedByCurrentUser = !post.isLikedByCurrentUser
                    )
                } else {
                    post
                }
            }
        }
    }

    fun loadComments(postId: String) {
        // Generate mock comments if not already loaded
        if (!_comments.value.containsKey(postId)) {
            val mockComments = generateMockComments(postId)
            _comments.update { currentComments ->
                currentComments + (postId to mockComments)
            }
        }
    }

    fun addComment(postId: String, content: String) {
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            postId = postId,
            userId = "current_user",
            username = "Me",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        
        _comments.update { currentComments ->
            val existingComments = currentComments[postId] ?: emptyList()
            currentComments + (postId to (listOf(newComment) + existingComments))
        }
        
        // Update comment count in posts
        _feedPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + 1)
                } else {
                    post
                }
            }
        }
    }

    private fun generateMockPosts(): List<Post> {
        val mockUsernames = listOf(
            "@the_real_ondra",
            "@alex_honald",
            "@jimmy_chin",
            "@ashima_shiraishi",
            "@adam_ondra",
            "@margo_hayes",
            "@chris_sharma",
            "@lynn_hill"
        )
        
        val mockLocations = listOf(
            "Red River Gorge, KY",
            "Yosemite Valley, CA",
            "Boulder Canyon, CO",
            "Smith Rock, OR",
            "Joshua Tree, CA",
            "The Gunks, NY",
            "Rumney, NH",
            "New River Gorge, WV"
        )
        
        val mockDescriptions = listOf(
            "EZ SEND. Yorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu turpis molestie, dictum est a, mattis tellus.",
            "Incredible line today! The beta was perfect and the holds were solid.",
            "Finally sent this project after months of work. The crux was absolutely brutal!",
            "Beautiful day for climbing. The weather was perfect and the rock was dry.",
            "New personal best on this route. The training is finally paying off!",
            "Epic session with great friends. This is what climbing is all about.",
            "The beta on this one was tricky but once I figured it out, it went down smooth.",
            "Amazing views from the top. Worth every second of the approach."
        )
        
        val mockMediaUrls = listOf(
            "https://picsum.photos/400/600?random=1",
            "https://picsum.photos/400/600?random=2",
            "https://picsum.photos/400/600?random=3",
            "https://picsum.photos/400/600?random=4",
            "https://picsum.photos/400/600?random=5"
        )
        
        return (1..50).map { index ->
            val timestamp = System.currentTimeMillis() - (index * 3600000L) // Each post 1 hour apart
            val date = java.text.SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
            
            Post(
                id = "post_$index",
                userId = "user_${index % 8}",
                username = mockUsernames[index % mockUsernames.size],
                userProfileImage = "https://picsum.photos/100/100?random=${index + 100}",
                title = "Route ${index}",
                location = mockLocations[index % mockLocations.size],
                date = date,
                timestamp = timestamp,
                vScale = Random.nextInt(1, 11),
                isIndoor = Random.nextBoolean(),
                notes = "Notes for route $index",
                description = mockDescriptions[index % mockDescriptions.size],
                mediaUrls = if (Random.nextBoolean()) listOf(mockMediaUrls[index % mockMediaUrls.size]) else emptyList(),
                likes = Random.nextInt(0, 1000),
                comments = Random.nextInt(0, 50),
                isLikedByCurrentUser = false
            )
        }.sortedByDescending { it.timestamp } // Newest first
    }

    private fun generateMockComments(postId: String): List<Comment> {
        val mockUsernames = listOf(
            "climber_pro",
            "boulder_crusher",
            "route_master",
            "send_it",
            "climbing_life"
        )
        
        val mockComments = listOf(
            "Incredible send! 🔥",
            "The beta looks perfect",
            "Wish I could climb like that",
            "Amazing line!",
            "Great work on this one",
            "The holds look so small from here",
            "Epic photo!",
            "This route is on my bucket list"
        )
        
        return (1..Random.nextInt(3, 8)).map { index ->
            val timestamp = System.currentTimeMillis() - (index * 600000L) // Each comment 10 minutes apart
            Comment(
                id = "comment_${postId}_$index",
                postId = postId,
                userId = "commenter_$index",
                username = mockUsernames[index % mockUsernames.size],
                userProfileImage = "https://picsum.photos/50/50?random=${index + 200}",
                content = mockComments[index % mockComments.size],
                timestamp = timestamp
            )
        }.sortedByDescending { it.timestamp } // Newest first
    }
}
