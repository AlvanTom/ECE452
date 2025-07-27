package com.example.ece452.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.ece452.data.Attempt
import com.example.ece452.data.Route
import com.example.ece452.navigation.Routes
import com.example.ece452.ui.theme.backgroundLight
import com.example.ece452.ui.theme.primaryContainerDark
import com.example.ece452.ui.theme.secondaryLight
import com.example.ece452.ui.theme.secondaryContainerLight
import com.example.ece452.ui.viewmodels.SessionViewModel
import java.util.UUID
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Delete

@Composable
fun RouteScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel,
    routeIdx: Int? = null // null means create mode, otherwise update mode
) {
    val session by sessionViewModel.activeSession.collectAsState()
    val existingRoute = routeIdx?.let { session?.routes?.getOrNull(it) }
    var routeName by remember { mutableStateOf(existingRoute?.routeName ?: "My Route") }
    var vScale by remember { mutableStateOf(existingRoute?.difficulty?.removePrefix("V")?.toFloatOrNull() ?: 4f) }
    var notes by remember { mutableStateOf(existingRoute?.notes ?: "") }
    var tags by remember { mutableStateOf(existingRoute?.tags ?: listOf()) }
    var tagInput by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(existingRoute?.attempts ?: emptyList()) }
    val scrollState = rememberScrollState()
    val isUpdateMode = routeIdx != null

    LaunchedEffect(existingRoute) {
        if (isUpdateMode && existingRoute != null) {
            routeName = existingRoute.routeName
            vScale = existingRoute.difficulty.removePrefix("V").toFloatOrNull() ?: 4f
            notes = existingRoute.notes ?: ""
            tags = existingRoute.tags
            attempts = existingRoute.attempts
        }
    }

    val context = LocalContext.current
    var selectedMediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showMediaSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraAction by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedMediaUris = listOf(uri)
        }
    }
    val cameraImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            selectedMediaUris = listOf(cameraImageUri!!)
        }
    }
    val cameraVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && cameraVideoUri != null) {
            selectedMediaUris = listOf(cameraVideoUri!!)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingCameraAction != null && pendingCameraUri != null) {
            if (pendingCameraAction == "photo") {
                cameraImageUri = pendingCameraUri
                cameraImageLauncher.launch(pendingCameraUri)
            } else if (pendingCameraAction == "video") {
                cameraVideoUri = pendingCameraUri
                cameraVideoLauncher.launch(pendingCameraUri)
            }
        }
        pendingCameraAction = null
        pendingCameraUri = null
    }
    fun isVideoFile(uri: Uri): Boolean {
        val path = uri.toString().lowercase()
        return path.endsWith(".mp4") || path.endsWith(".3gp") || path.endsWith(".mkv") || path.contains("video")
    }
    @Composable
    fun rememberVideoThumbnail(context: android.content.Context, uri: Uri): Bitmap? {
        return remember(uri) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val bitmap = retriever.getFrameAtTime(0)
                retriever.release()
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(backgroundLight)
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = if (isUpdateMode) "Edit Route" else "New Route",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Route Name Field
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Route Name") },
                    placeholder = { Text("e.g. Main Wall V3") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // V Difficulty Field
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("Add Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val newTag = tagInput.trim()
                                if (newTag.isNotEmpty() && !tags.contains(newTag)) {
                                    tags = tags + newTag
                                    tagInput = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add tag"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tags Display
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { tags = tags - tag },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "V-Scale: V${vScale.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Slider(
                        value = vScale,
                        onValueChange = { vScale = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    singleLine = false,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (isUpdateMode && routeIdx != null) {
                            // Add attempt to existing route
                            val updatedRoute = existingRoute?.copy(
                                routeName = routeName,
                                difficulty = "V${vScale.toInt()}",
                                notes = notes,
                                tags = tags,
                                attempts = attempts // keep attempts
                            )
                            if (updatedRoute != null) {
                                sessionViewModel.updateRoute(routeIdx, updatedRoute)
                            }
                            navController.navigate("Attempt/$routeIdx")
                        } else {
                            val newRoute = Route(
                                id = UUID.randomUUID().toString(),
                                routeName = routeName,
                                difficulty = "V${vScale.toInt()}",
                                notes = notes,
                                tags = tags,
                                attempts = emptyList<Attempt>()
                            )
                            sessionViewModel.addRouteToActiveSession(newRoute)
                            navController.navigate(Routes.Attempt.name)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryLight),
                    enabled = routeName.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Attempt",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isUpdateMode) "Add Attempt" else "Add Attempt", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- MEDIA UPLOAD UI ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            mediaPickerLauncher.launch("*/*")
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryContainerLight,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Choose from Library",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Library")
                    }
                    Button(
                        onClick = { showMediaSourceDialog = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryContainerLight,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Camera",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Camera")
                    }
                }
                if (showMediaSourceDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showMediaSourceDialog = false },
                        title = { Text("Camera Action") },
                        text = { Text("Take a photo or record a video:") },
                        confirmButton = {
                            Column {
                                androidx.compose.material3.TextButton(onClick = {
                                    showMediaSourceDialog = false
                                    val photoFile = java.io.File(
                                        context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
                                        "camera_photo_${System.currentTimeMillis()}.jpg"
                                    )
                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".provider", photoFile)
                                    pendingCameraAction = "photo"
                                    pendingCameraUri = uri
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }) { Text("Take Photo") }
                                androidx.compose.material3.TextButton(onClick = {
                                    showMediaSourceDialog = false
                                    val videoFile = java.io.File(
                                        context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES),
                                        "camera_video_${System.currentTimeMillis()}.mp4"
                                    )
                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".provider", videoFile)
                                    pendingCameraAction = "video"
                                    pendingCameraUri = uri
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }) { Text("Record Video") }
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showMediaSourceDialog = false }) { Text("Cancel") }
                        }
                    )
                }
                if (selectedMediaUris.isNotEmpty()) {
                    Text("Selected Media:", style = MaterialTheme.typography.bodyLarge)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        selectedMediaUris.forEach { uri ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp)
                            ) {
                                if (isVideoFile(uri)) {
                                    val thumbnail = rememberVideoThumbnail(context, uri)
                                    if (thumbnail != null) {
                                        Image(
                                            bitmap = thumbnail.asImageBitmap(),
                                            contentDescription = "Video thumbnail",
                                            modifier = Modifier.matchParentSize().clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = "Video",
                                            modifier = Modifier.matchParentSize()
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(36.dp)
                                            .drawBehind {
                                                drawCircle(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    radius = size.minDimension / 2
                                                )
                                            }
                                    )
                                } else {
                                    Image(
                                        painter = coil.compose.rememberAsyncImagePainter(uri),
                                        contentDescription = null,
                                        modifier = Modifier.matchParentSize().clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = { selectedMediaUris = emptyList() },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Media",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
                // --- END MEDIA UPLOAD UI ---

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isUpdateMode && routeIdx != null) {
                            val updatedRoute = existingRoute?.copy(
                                routeName = routeName,
                                difficulty = "V${vScale.toInt()}",
                                notes = notes,
                                tags = tags,
                                attempts = attempts
                            )
                            if (updatedRoute != null) {
                                sessionViewModel.updateRoute(routeIdx, updatedRoute)
                            }
                            navController.popBackStack()
                        } else {
                            val newRoute = Route(
                                id = UUID.randomUUID().toString(),
                                routeName = routeName,
                                difficulty = "V${vScale.toInt()}",
                                notes = notes,
                                tags = tags,
                                attempts = emptyList<Attempt>()
                            )
                            sessionViewModel.addRouteToActiveSession(newRoute)
                            navController.popBackStack()
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryContainerDark),
                    enabled = routeName.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = if (isUpdateMode) "Update Route" else "Save Route",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isUpdateMode) "Update Route" else "Save Route", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(Routes.ActiveSession.name) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back to Active Session",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Back to Active Session", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    )
}