package com.example.ece452.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String = "Climbr",
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp + 16.dp) // Add extra height for top padding
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp) // Add top padding to avoid status bar
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B6536),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onProfileClick) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF3F1F6),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFFB0AEB8), modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
    }
} 