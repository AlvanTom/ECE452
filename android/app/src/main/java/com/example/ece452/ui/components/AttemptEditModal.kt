package com.example.ece452.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ece452.data.Attempt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttemptEditModal(
    show: Boolean,
    attempt: Attempt?,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show && attempt != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Attempt", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status:")
                    Spacer(Modifier.width(16.dp))
                    // Success Toggle
                    Button(
                        onClick = { onStatusChange(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (attempt.success) Color(0xFF4B6536) else Color.LightGray,
                            contentColor = if (attempt.success) Color.White else Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Success")
                    }
                    Spacer(Modifier.width(8.dp))
                    // Fail Toggle
                    Button(
                        onClick = { onStatusChange(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!attempt.success) Color(0xFFB00020) else Color.LightGray,
                            contentColor = if (!attempt.success) Color.White else Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Fail"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Fail")
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Attempt")
                    }
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Attempt")
                    }
                }
            }
        }
    }
} 