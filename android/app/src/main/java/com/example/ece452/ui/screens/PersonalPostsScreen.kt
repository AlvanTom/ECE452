package com.example.ece452.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ece452.ui.components.*
import com.example.ece452.ui.theme.*
import com.example.ece452.ui.viewmodels.PostViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalPostsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel = viewModel()
) {
    // Check if user is verified
    val currentUser = Firebase.auth.currentUser
    val isVerifiedUser = currentUser?.email == "fiontest@gmail.com"
    
    val personalPosts by postViewModel.personalPosts.collectAsState()
    val comments by postViewModel.comments.collectAsState()
    
    var showCommentsModal by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Posts",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (!isVerifiedUser) {
            // Show verification message for non-verified users
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(backgroundLight)
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Become a verified user in order to post!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Show normal posts screen for verified users
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(backgroundLight)
                    .padding(innerPadding)
            ) {
                if (personalPosts.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Posts Yet",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first post to see it here!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("Posts") }
                        ) {
                            Text("Create Post")
                        }
                    }
                }
            } else {
                // Posts list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(personalPosts) { post ->
                        PostCard(
                            post = post,
                            onLikeClick = {
                                postViewModel.toggleLike(post.id)
                            },
                            onCommentClick = {
                                selectedPostId = post.id
                                postViewModel.loadComments(post.id)
                                showCommentsModal = true
                            }
                        )
                    }
                }
            }
        }
    }
    }
    
    // Comments modal
    if (showCommentsModal && selectedPostId != null) {
        val postComments = comments[selectedPostId] ?: emptyList()
        CommentsModal(
            comments = postComments,
            onDismiss = {
                showCommentsModal = false
                selectedPostId = null
            },
            onAddComment = { commentText ->
                selectedPostId?.let { postId ->
                    postViewModel.addComment(postId, commentText)
                }
            }
        )
    }
} 