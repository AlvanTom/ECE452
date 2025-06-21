package com.example.ece452.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ece452.data.Route
import com.example.ece452.data.Session
import com.example.ece452.firebase.AuthService
import com.example.ece452.firebase.FunctionsService
import com.example.ece452.navigation.Routes
import com.example.ece452.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    navController: NavController,
    sessionId: String? = null
) {
    var session by remember { mutableStateOf<Session?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val functionsService = remember { FunctionsService() }
    val authService = remember { AuthService() }
    
    LaunchedEffect(sessionId) {
        // Existing data fetching logic
        try {
            if (sessionId != null) {
                val result = functionsService.getSessionByID(sessionId)
                result.fold(
                    onSuccess = { data ->
                        if (data != null) {
                            val sessionData = data["sessionData"] as? Map<String, Any>
                            val routesData = data["routesData"] as? List<Map<String, Any>>

                            if (sessionData != null) {
                                session = Session(
                                    id = sessionId,
                                    userId = sessionData["userId"] as? String ?: "",
                                    title = sessionData["title"] as? String ?: "",
                                    location = sessionData["location"] as? String ?: "",
                                    isIndoor = sessionData["isIndoor"] as? Boolean ?: true,
                                    gymName = sessionData["gymName"] as? String,
                                    createdAt = sessionData["createdAt"] as? String ?: "",
                                    routes = routesData?.map { routeMap ->
                                        val attempts = routeMap["attempts"] as? List<Map<String, Any>> ?: emptyList()
                                        com.example.ece452.data.Route(
                                            id = routeMap["id"] as? String ?: "",
                                            routeName = routeMap["routeName"] as? String ?: "",
                                            difficulty = routeMap["difficulty"] as? String ?: "",
                                            tags = (routeMap["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                            notes = routeMap["notes"] as? String,
                                            attempts = attempts.map { attemptMap ->
                                                com.example.ece452.data.Attempt(
                                                    success = attemptMap["success"] as? Boolean ?: false,
                                                    createdAt = attemptMap["createdAt"] as? String ?: ""
                                                )
                                            }
                                        )
                                    } ?: emptyList()
                                )
                            }
                        }
                    },
                    onFailure = { exception ->
                        error = exception.message ?: "Failed to load session"
                    }
                )
            } else {
                // This case should ideally not be hit in the final flow
                // as navigation to this screen should always provide a session ID.
                // We'll show an error or an empty state.
                error = "No session ID provided."
            }
        } catch (e: Exception) {
            error = e.message ?: "Unknown error occurred"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundLight)
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                session == null -> {
                    // This case is now mainly for when a real session isn't found
                    Text(
                        text = "Session not found.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = session!!.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(session!!.routes) { route ->
                                RouteListItem(route = route)
                                Divider()
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                navController.navigate(Routes.Route.name)
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add a new route")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add a new route")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { /* TODO: End session logic */ },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.Output, contentDescription = "End Session")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Session")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteListItem(route: Route) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Implement expand/collapse logic */ }
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "${route.attempts.size} Attempts",
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = route.routeName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        // Placeholder for the collapsible list of attempts
        // For now, it's just empty space as requested.
        // We will implement the attempt list here later.
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
fun RouteCard(route: com.example.ece452.data.Route) {
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