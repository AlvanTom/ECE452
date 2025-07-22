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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ece452.ui.theme.backgroundLight
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserProfileScreen() {
    val user = Firebase.auth.currentUser
    val scrollState = rememberScrollState()

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

    var postCount by remember { mutableStateOf(0) }
    var sessionCount by remember { mutableStateOf(0) }
    var uniqueClimbDays by remember { mutableStateOf(0) }

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

                    val now = System.currentTimeMillis()
                    val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000L
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    val uniqueDays = documents.mapNotNull { doc ->
                        val timestamp = doc.getTimestamp("date")?.toDate()?.time
                        if (timestamp != null && timestamp >= sevenDaysAgo) {
                            sdf.format(timestamp)
                        } else null
                    }.toSet()

                    uniqueClimbDays = uniqueDays.size
                }
        }
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
                    Text("Days climbed (last 7d): $uniqueClimbDays")
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
    }
}