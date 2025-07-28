package com.example.ece452.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import coil.compose.rememberAsyncImagePainter
import com.example.ece452.ui.theme.backgroundLight
import com.example.ece452.firebase.FirebaseConfig
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import android.os.Environment
import android.Manifest
import java.io.File
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ece452.ui.viewmodels.UserProfileViewModel
import com.example.ece452.firebase.FunctionsService

@Composable
fun UserProfileScreen(
    userProfileViewModel: UserProfileViewModel? = null
) {
    val viewModel = userProfileViewModel ?: viewModel<UserProfileViewModel>()
    val user = Firebase.auth.currentUser
    val scrollState = rememberScrollState()
    val profilePhotoUrl by viewModel.profilePhotoUrl.collectAsState()

    val db = Firebase.firestore
    val uid = user?.uid

    var showEditDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf(user?.displayName ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatchError by remember { mutableStateOf<String?>(null) }

    var feedback by remember { mutableStateOf<String?>(null) }

    // Profile photo state
    var selectedProfilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraAction by remember { mutableStateOf<String?>(null) }

    var postCount by remember { mutableStateOf(0) }
    var sessionCount by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Launchers for photo selection and camera
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedProfilePhotoUri = uri
        }
    }

    val cameraImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            selectedProfilePhotoUri = cameraImageUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingCameraAction != null && pendingCameraUri != null) {
            cameraImageUri = pendingCameraUri
            cameraImageLauncher.launch(pendingCameraUri)
        }
        pendingCameraAction = null
        pendingCameraUri = null
    }

    // Function to upload profile photo
    fun uploadProfilePhoto(uri: Uri, userId: String) {
        coroutineScope.launch {
            isUploadingPhoto = true
            try {
                val storage = FirebaseConfig.storage
                val fileName = "profile_photo_${userId}_${System.currentTimeMillis()}.jpg"
                val ref: StorageReference = storage.reference.child("profile_photos/$fileName")
                
                // Set metadata with userId for security rules
                val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                    .setCustomMetadata("userId", userId)
                    .build()
                
                val uploadTask = ref.putFile(uri, metadata).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                
                // Update user profile in Firebase Auth
                val currentUser = Firebase.auth.currentUser
                currentUser?.let {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(downloadUrl))
                        .build()
                    it.updateProfile(profileUpdates).await()
                }
                
                // Update user profile in Firestore via backend function
                val functionsService = FunctionsService()
                val updateResult = functionsService.updateUser(profilePhotoUrl = downloadUrl)
                
                updateResult.fold(
                    onSuccess = { success ->
                        if (success) {
                            feedback = "Profile photo updated successfully!"
                            selectedProfilePhotoUri = null // Clear selection
                            viewModel.updateProfilePhoto(downloadUrl) // Update ViewModel
                        } else {
                            feedback = "Failed to update profile in database"
                        }
                    },
                    onFailure = { exception ->
                        feedback = "Failed to update profile: ${exception.message}"
                    }
                )
                
                isUploadingPhoto = false
            } catch (e: Exception) {
                feedback = "Failed to upload photo: ${e.message}"
                isUploadingPhoto = false
            }
        }
    }

    LaunchedEffect(uid) {
        uid?.let { userId ->
            db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    postCount = documents.size()
                }

            db.collection("sessions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    sessionCount = documents.size()
                }
        }
    }

    // Refresh profile photo when user changes
    LaunchedEffect(user?.uid) {
        viewModel.refreshProfilePhoto()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(backgroundLight)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile photo or placeholder
                if (selectedProfilePhotoUri != null) {
                    // Show selected photo
                    Image(
                        painter = rememberAsyncImagePainter(selectedProfilePhotoUri),
                        contentDescription = "Selected Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (profilePhotoUrl != null) {
                    // Show existing profile photo from ViewModel
                    Image(
                        painter = rememberAsyncImagePainter(profilePhotoUrl),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Show placeholder
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user?.displayName?.take(1)?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }

                // Upload button overlay
                IconButton(
                    onClick = { showPhotoOptionsDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Loading indicator
                if (isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Upload button for selected photo
            if (selectedProfilePhotoUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { 
                            uploadProfilePhoto(selectedProfilePhotoUri!!, user?.uid ?: "")
                        },
                        enabled = !isUploadingPhoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Upload Photo")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { 
                            selectedProfilePhotoUri = null
                            // Clear any pending camera state
                            cameraImageUri = null
                            pendingCameraUri = null
                            pendingCameraAction = null
                        },
                        enabled = !isUploadingPhoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Account Info", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Username", style = MaterialTheme.typography.labelMedium)
                    Text(text = user?.displayName ?: "Not set", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Email", style = MaterialTheme.typography.labelMedium)
                    Text(text = user?.email ?: "Not set", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Info")
                Spacer(Modifier.width(8.dp))
                Text("Update Username")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showChangePasswordDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Change Password")
                Spacer(Modifier.width(8.dp))
                Text("Change Password")
            }

            feedback?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Climbing Stats", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Number of posts: $postCount")
                    Text("Number of sessions: $sessionCount")
                }
            }
        }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        val currentUser = Firebase.auth.currentUser
                        currentUser?.let {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(newUsername)
                                .build()
                            it.updateProfile(profileUpdates)
                                .addOnSuccessListener {
                                    feedback = "Username updated successfully!"
                                    showEditDialog = false
                                }
                                .addOnFailureListener { e ->
                                    feedback = "Username update failed: ${e.message}"
                                }
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Update Username") },
                text = {
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("New Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showChangePasswordDialog) {
            AlertDialog(
                onDismissRequest = { showChangePasswordDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        if (newPassword != confirmPassword) {
                            passwordMismatchError = "Passwords do not match."
                            return@TextButton
                        }

                        val email = user?.email
                        if (!email.isNullOrEmpty()) {
                            val credential = EmailAuthProvider.getCredential(email, currentPassword)
                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            feedback = "Password updated successfully."
                                            showChangePasswordDialog = false
                                            passwordMismatchError = null
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                        }
                                        .addOnFailureListener { e ->
                                            feedback = "Password update failed: ${e.message}"
                                        }
                                }
                                .addOnFailureListener { e ->
                                    feedback = "Re-authentication failed: ${e.message}"
                                }
                        }
                    }) {
                        Text("Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePasswordDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Change Password") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        passwordMismatchError?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Photo options dialog
        if (showPhotoOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoOptionsDialog = false },
                title = { Text("Choose Photo") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch("image/*")
                                showPhotoOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Choose from Gallery")
                        }
                        
                        TextButton(
                            onClick = {
                                val photoFile = File.createTempFile(
                                    "profile_photo_${System.currentTimeMillis()}",
                                    ".jpg",
                                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                )
                                pendingCameraUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    photoFile
                                )
                                pendingCameraAction = "take_photo"
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                showPhotoOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Camera",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Take Photo")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPhotoOptionsDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}