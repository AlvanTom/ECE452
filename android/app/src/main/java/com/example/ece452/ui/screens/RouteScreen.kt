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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.ece452.data.Attempt
import com.example.ece452.data.Route
import com.example.ece452.navigation.Routes
import com.example.ece452.ui.theme.backgroundLight
import com.example.ece452.ui.theme.primaryContainerLight
import com.example.ece452.ui.viewmodels.SessionViewModel
import java.util.UUID

@Composable
fun RouteScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel
) {

    var routeName by remember { mutableStateOf("") }
    var selectedVDifficulty by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val vScaleOptions = (0..12).map { "V$it" }
    var notes by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var tagInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

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
                    text = "New Route",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Route Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    readOnly = true,
                    value = selectedVDifficulty?.let { "V$it" } ?: "Select V Difficulty",
                    onValueChange = {},
                    label = { Text("V Difficulty") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Dropdown Icon"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = true,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

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

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    vScaleOptions.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedVDifficulty = index
                                expanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp) // multiline height
                        .padding(vertical = 8.dp),
                    singleLine = false,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val newRoute = Route(
                            id = UUID.randomUUID().toString(),
                            routeName = routeName,
                            difficulty = selectedVDifficulty?.let { "V$it" } ?: "",
                            notes = notes,
                            tags = tags,
                            attempts = emptyList<Attempt>()
                        )
                        sessionViewModel.addRouteToActiveSession(newRoute)
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryContainerLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Route",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add Route", fontSize = 16.sp)
                }
            }
        }
    )
}