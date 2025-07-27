package com.example.ece452.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ece452.R
import com.example.ece452.ui.viewmodels.UserProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String = "Climbr",
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    userProfileViewModel: UserProfileViewModel? = null
) {
    val viewModel = userProfileViewModel ?: viewModel<UserProfileViewModel>()
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp + 16.dp) // Add extra height for top padding
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp) // Add top padding to avoid status bar
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Climbr Logo",
                    modifier = Modifier
                        .height(32.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onProfileClick) {
                    val profilePhotoUrl by viewModel.profilePhotoUrl.collectAsState()
                    val currentUser = Firebase.auth.currentUser
                    
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF3F1F6),
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (profilePhotoUrl != null) {
                            // Show actual profile photo
                            Image(
                                painter = rememberAsyncImagePainter(profilePhotoUrl),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Show placeholder with user's initial
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.displayName?.take(1)?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFB0AEB8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 