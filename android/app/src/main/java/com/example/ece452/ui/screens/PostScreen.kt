package com.example.ece452.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ece452.ui.theme.*
import java.text.SimpleDateFormat
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ece452.ui.viewmodels.PostViewModel
import android.Manifest

import java.util.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.core.content.FileProvider
import java.io.File
import android.os.Environment
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Videocam
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.filled.Delete


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(postViewModel: PostViewModel = viewModel()) {

    // Currently no backend integration
    // Currently no media upload

    var title by remember { mutableStateOf("Title") }
    var location by remember { mutableStateOf("Location") }
    var vScale by remember { mutableStateOf(4f) } // default to V4
    val currentDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
    var date by remember { mutableStateOf(currentDate) }
    var isIndoor by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }


    val datePickerState = rememberDatePickerState()
    val scrollState = rememberScrollState()
    val showDatePicker = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedMediaUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var showMediaSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraAction by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers for camera actions
    val cameraImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            selectedMediaUris = listOf(cameraImageUri!!) // Overwrite with new photo
        }
    }
    val cameraVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && cameraVideoUri != null) {
            selectedMediaUris = listOf(cameraVideoUri!!) // Overwrite with new video
        }
    }

    // Permission launcher (declare this AFTER the above)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingCameraAction != null && pendingCameraUri != null) {
            if (pendingCameraAction == "photo") {
                cameraImageUri = pendingCameraUri
                cameraImageLauncher.launch(pendingCameraUri)
            } else if (pendingCameraAction == "video") {
                cameraVideoUri = pendingCameraUri // CRITICAL: set before launching
                cameraVideoLauncher.launch(pendingCameraUri)
            }
        }
        pendingCameraAction = null
        pendingCameraUri = null
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedMediaUris = listOf(uri) // Only one file at a time
        }
    }

    // Helper function to check if a URI is a video file
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
                    .verticalScroll(scrollState)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "New Post",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    trailingIcon = {
                        IconButton(onClick = { title = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    trailingIcon = {
                        IconButton(onClick = { location = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { isIndoor = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIndoor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isIndoor) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Indoor")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { isIndoor = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIndoor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (!isIndoor) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Outdoor")
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker.value = true }) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Select Date"
                            )
                        }
                    },
                    readOnly = true
                )

                // V-Scale Slider
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    trailingIcon = {
                        if (notes.isNotEmpty()) {
                            IconButton(onClick = { notes = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    maxLines = 5,
                    singleLine = false
                )

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
                                        painter = rememberAsyncImagePainter(uri),
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

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            // Launch gallery picker for a single image or video
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

                Spacer(modifier = Modifier.height(24.dp))

                if (showMediaSourceDialog) {
                    AlertDialog(
                        onDismissRequest = { showMediaSourceDialog = false },
                        title = { Text("Camera Action") },
                        text = { Text("Take a photo or record a video:") },
                        confirmButton = {
                            Column {
                                TextButton(onClick = {
                                    showMediaSourceDialog = false
                                    val photoFile = File(
                                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                        "camera_photo_${System.currentTimeMillis()}.jpg"
                                    )
                                    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", photoFile)
                                    pendingCameraAction = "photo"
                                    pendingCameraUri = uri
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }) { Text("Take Photo") }
                                TextButton(onClick = {
                                    showMediaSourceDialog = false
                                    val videoFile = File(
                                        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                                        "camera_video_${System.currentTimeMillis()}.mp4"
                                    )
                                    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", videoFile)
                                    pendingCameraAction = "video"
                                    pendingCameraUri = uri
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }) { Text("Record Video") }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showMediaSourceDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                if (uploadError != null) {
                    Text(uploadError!!, color = MaterialTheme.colorScheme.error)
                }
                if (isUploading) {
                    CircularProgressIndicator()
                }
                Button(
                    onClick = {
                        isUploading = true
                        uploadError = null
                        coroutineScope.launch {
                            try {
                                val userId = "user" // TODO: Replace with actual user ID from auth
                                val urls = postViewModel.uploadMediaFiles(selectedMediaUris, userId)
                                postViewModel.createPost(
                                    title = title,
                                    location = location,
                                    date = date,
                                    vScale = vScale.toInt(),
                                    isIndoor = isIndoor,
                                    notes = notes,
                                    description = notes,
                                    mediaUrls = urls
                                )
                                postViewModel.saveActivePost()
                                selectedMediaUris = emptyList()
                            } catch (e: Exception) {
                                uploadError = e.localizedMessage ?: "Failed to upload media."
                            } finally {
                                isUploading = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isUploading
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Post",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isUploading) "Posting..." else "Post!", fontSize = 16.sp)
                }
            }
        }
    )

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker.value = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        date = formatter.format(Date(millis))
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
