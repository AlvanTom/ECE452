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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ece452.navigation.Routes
import com.example.ece452.ui.theme.backgroundLight
import com.example.ece452.ui.theme.primaryLight
import com.example.ece452.ui.viewmodels.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel,
    sessionId: String? = null // <-- new argument for update mode
) {
    val sessionHistory by sessionViewModel.sessionHistory.collectAsState()
    val sessionToEdit = sessionHistory.find { it.id == sessionId } // find session if editing
    var title by remember { mutableStateOf(sessionToEdit?.title ?: "Title") }
    var gym by remember { mutableStateOf(sessionToEdit?.gymName ?: "Gym") }
    var wallName by remember { mutableStateOf(sessionToEdit?.location?.substringAfter("- ") ?: "Wall Name") }
    val currentDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())
    var date by remember { mutableStateOf(sessionToEdit?.createdAt ?: currentDate) }

    val datePickerState = rememberDatePickerState()
    val showDatePicker = remember { mutableStateOf(false) }

    val isUpdateMode = sessionToEdit != null
    var isUpdating by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(backgroundLight)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isUpdateMode) "Edit Session" else "New Session",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                if (updateError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = updateError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { updateError = null }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
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
                    value = gym,
                    onValueChange = { gym = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    trailingIcon = {
                        IconButton(onClick = { gym = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = wallName,
                    onValueChange = { wallName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    trailingIcon = {
                        IconButton(onClick = { wallName = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                )
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
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (isUpdateMode && sessionToEdit != null) {
                            val updatedSession = sessionToEdit.copy(
                                title = title,
                                gymName = gym,
                                location = if (gym.isNotBlank() && wallName.isNotBlank()) "$gym - $wallName" else gym.ifBlank { wallName },
                                createdAt = date
                            )
                            isUpdating = true
                            updateError = null
                            scope.launch {
                                val result = sessionViewModel.updateSession(updatedSession)
                                isUpdating = false
                                result.fold(
                                    onSuccess = {
                                        navController.navigate(Routes.Sessions.name) { popUpTo(Routes.Sessions.name) { inclusive = true } }
                                    },
                                    onFailure = { exception ->
                                        updateError = exception.message ?: "Unknown error"
                                    }
                                )
                            }
                        } else {
                            sessionViewModel.createSession(title, gym, wallName)
                            navController.navigate(Routes.ActiveSession.name)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryLight),
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Updating...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (isUpdateMode) "Update Session" else "Start Session",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isUpdateMode) "Update Session" else "Start Session", fontSize = 16.sp)
                    }
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