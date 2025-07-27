package com.example.ece452.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ece452.ui.components.*
import com.example.ece452.ui.theme.*
import com.example.ece452.ui.viewmodels.PostViewModel

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel = viewModel()
) {
    val feedPosts by postViewModel.feedPosts.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()
    val hasMorePosts by postViewModel.hasMorePosts.collectAsState()
    val comments by postViewModel.comments.collectAsState()
    
    var showCommentsModal by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    
    val listState = rememberLazyListState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        // Feed content
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(feedPosts) { post ->
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
            
            // Load more button
            if (hasMorePosts) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { postViewModel.loadMorePosts() },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isLoading) "Loading..." else "Load More Posts"
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