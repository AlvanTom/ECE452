package com.example.ece452.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image

@Composable
fun MediaViewer(
    mediaUri: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Helper function to check if a URI is a video file
    fun isVideoFile(uri: String): Boolean {
        val path = uri.lowercase()
        return path.endsWith(".mp4") || path.endsWith(".3gp") || path.endsWith(".mkv") || path.contains("video")
    }
    
    // Helper function to get video thumbnail
    @Composable
    fun rememberVideoThumbnail(context: android.content.Context, uri: String): Bitmap? {
        return remember(uri) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(uri)
                val bitmap = retriever.getFrameAtTime(0)
                retriever.release()
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() }
        ) {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            // Media content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isVideoFile(mediaUri)) {
                    // Show video placeholder with play button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val thumbnail = rememberVideoThumbnail(context, mediaUri)
                        if (thumbnail != null) {
                            Image(
                                bitmap = thumbnail.asImageBitmap(),
                                contentDescription = "Video thumbnail",
                                modifier = Modifier
                                    .size(300.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(300.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Video",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Play button
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color.Black.copy(alpha = 0.7f),
                                        radius = size.minDimension / 2
                                    )
                                }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Video (tap to play)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                } else {
                    // Show image
                    AsyncImage(
                        model = mediaUri,
                        contentDescription = "Media",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
} 