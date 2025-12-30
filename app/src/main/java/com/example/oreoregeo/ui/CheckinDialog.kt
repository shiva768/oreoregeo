package com.example.oreoregeo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckinDialog(
    placeKey: String,
    placeName: String?,
    checkinState: CheckinState,
    onCheckin: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var note by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(checkinState) {
        when (checkinState) {
            is CheckinState.Loading -> isButtonEnabled = false
            is CheckinState.Success -> {
                // Keep dialog open briefly to show success, then dismiss
                kotlinx.coroutines.delay(1000)
                onDismiss()
            }
            is CheckinState.Error -> isButtonEnabled = true
            is CheckinState.Idle -> isButtonEnabled = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Check-in") },
        text = {
            Column {
                Text("Place: ${placeName ?: placeKey}")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                
                when (checkinState) {
                    is CheckinState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is CheckinState.Success -> {
                        Text(
                            "Check-in successful!",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is CheckinState.Error -> {
                        Text(
                            checkinState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is CheckinState.Idle -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCheckin(note) },
                enabled = isButtonEnabled
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Check-in")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
