package com.example.ece452.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.ece452.data.Attempt
import com.example.ece452.data.Route
import com.example.ece452.navigation.Routes
import com.example.ece452.ui.theme.*
import com.example.ece452.ui.viewmodels.SessionViewModel
import kotlinx.coroutines.launch
import com.example.ece452.ui.components.AttemptEditModal
import androidx.compose.material.icons.filled.Edit
import com.example.ece452.data.Session
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel
) {
    val session by sessionViewModel.activeSession.collectAsState()
    val isEndingSession by sessionViewModel.isEndingSession.collectAsState()
    val errorMessage by sessionViewModel.errorMessage.collectAsState()
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var selectedAttempt: Triple<Int, Int, Attempt>? by remember { mutableStateOf(null) } // routeIdx, attemptIdx, attempt
    var editSuccess by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundLight)
                .padding(innerPadding)
        ) {
            if (session == null) {
                Text(
                    text = "No active session found.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Error message display
                    errorMessage?.let { message ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { sessionViewModel.clearError() }) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }

                    Text(
                        text = session?.title ?: "Unknown Session",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    session?.let { nonNullSession ->
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(nonNullSession.routes) { routeIdx, route ->
                                RouteListItem(
                                    route = route,
                                    isExpanded = expandedIndex == routeIdx,
                                    onClick = {
                                        expandedIndex = if (expandedIndex == routeIdx) null else routeIdx
                                    },
                                    onAttemptClick = { attemptIdx, attempt ->
                                        selectedAttempt = Triple(routeIdx, attemptIdx, attempt)
                                        editSuccess = attempt.success
                                        showSheet = true
                                    },
                                    onEditClick = {
                                        navController.navigate("${Routes.Route.name}/$routeIdx")
                                    }
                                )
                                Divider()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            navController.navigate(Routes.Route.name)
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        enabled = !isEndingSession
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add a new route", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add a new route", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val success = sessionViewModel.endActiveSession()
                                if (success) {
                                    navController.navigate(Routes.Sessions.name) {
                                        popUpTo(Routes.Sessions.name) { inclusive = true }
                                    }
                                }
                                // If failed, error message will be shown and session preserved
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        enabled = !isEndingSession
                    ) {
                        if (isEndingSession) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ending Session...")
                        } else {
                            Icon(Icons.Default.Output, contentDescription = "End Session")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Session")
                        }
                    }
                }
                AttemptEditModal(
                    show = showSheet && selectedAttempt != null,
                    attempt = selectedAttempt?.third?.copy(success = editSuccess ?: false),
                    onStatusChange = { editSuccess = it },
                    onDelete = {
                        selectedAttempt?.let { (routeIdx, attemptIdx, _) ->
                            sessionViewModel.deleteAttempt(routeIdx, attemptIdx)
                        }
                        showSheet = false
                    },
                    onSave = {
                        selectedAttempt?.let { (routeIdx, attemptIdx, _) ->
                            if (editSuccess != null) {
                                sessionViewModel.updateAttemptStatus(routeIdx, attemptIdx, editSuccess!!)
                            }
                        }
                        showSheet = false
                    },
                    onDismiss = { showSheet = false }
                )
            }
        }
    }
}

@Composable
fun RouteListItem(route: Route, isExpanded: Boolean, onClick: () -> Unit, onAttemptClick: (Int, Attempt) -> Unit, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Grouped background for attempts and route name
        Row(
            modifier = Modifier
                .then(if (isExpanded) Modifier.background(Color(0xFFE9E9DD)) else Modifier)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${route.attempts.size} Attempt${if (route.attempts.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                )
                Text(
                    text = route.routeName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp, top = 2.dp)
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Route")
            }
        }
        if (isExpanded) {
            // Show media if available
            route.mediaUri?.let { mediaUri ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = mediaUri,
                        contentDescription = "Route media",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            route.attempts.forEachIndexed { idx, attempt ->
                AttemptListItem(attempt, idx) { onAttemptClick(idx, attempt) }
            }
        }
    }
}

@Composable
fun AttemptListItem(attempt: Attempt, idx: Int, onClick: () -> Unit) {
    val timeAgo = remember(attempt.createdAt) {
        try {
            val created = Instant.parse(attempt.createdAt)
            val now = Instant.now()
            val duration = Duration.between(created, now)
            val seconds = duration.seconds
            val minutes = duration.toMinutes()
            val hours = duration.toHours()
            val days = duration.toDays()
            val weeks = days / 7
            val years = days / 365
            when {
                seconds < 5 -> "just now"
                seconds < 60 -> "$seconds seconds ago"
                minutes < 60 -> "$minutes minutes ago"
                hours < 24 -> "$hours hours ago"
                days < 7 -> "$days days ago"
                weeks < 52 -> "$weeks weeks ago"
                else -> "$years years ago"
            }
        } catch (e: DateTimeParseException) {
            ""
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = if (attempt.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = if (attempt.success) "Success" else "Failed",
            tint = if (attempt.success) Color(0xFF4B6536) else Color(0xFFB00020),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Attempt ${idx + 1}",
                style = MaterialTheme.typography.bodyLarge,
            )
            if (timeAgo.isNotEmpty()) {
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Details",
            tint = Color.Gray
        )
    }
}

@Composable
fun SessionHeader(session: Session) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (session.isIndoor) Icons.Default.Home else Icons.Default.Park,
                    contentDescription = if (session.isIndoor) "Indoor" else "Outdoor",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = session.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (session.gymName != null) {
                Text(
                    text = session.gymName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Routes: ${session.routes.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun RouteCard(route: Route) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = route.routeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Difficulty: ${route.difficulty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                AssistChip(
                    onClick = { /* Add attempt logic */ },
                    label = { Text("Add Attempt") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Attempt",
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            
            if (route.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    route.tags.take(3).forEach { tag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.bodySmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
            
            if (route.notes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = route.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (route.attempts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Attempts (${route.attempts.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    route.attempts.forEach { attempt ->
                        Icon(
                            imageVector = if (attempt.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = if (attempt.success) "Success" else "Failed",
                            tint = if (attempt.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyRoutesCard() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Route,
                contentDescription = "No Routes",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No routes yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first route to start tracking your climbs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 