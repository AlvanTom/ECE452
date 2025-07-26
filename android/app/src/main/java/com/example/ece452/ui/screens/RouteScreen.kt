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
import com.example.ece452.ui.viewmodels.SessionViewModel
import java.util.UUID

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

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(backgroundLight)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isUpdateMode) "Edit Route" else "New Route",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Route Name") },
                    placeholder = { Text("e.g. Main Wall V3") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = tag)
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { tags = tags - tag },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier.size(16.dp)
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), // multiline height
                    singleLine = false,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

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
            }
        }
    )
}