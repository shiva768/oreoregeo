package com.example.oreoregeo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditTagsScreen(
    placeKey: String,
    existingTags: Map<String, String>,
    onSave: (Long, Map<String, String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Extract node ID from place_key (osm:node:12345)
    val nodeId = placeKey.split(":").lastOrNull()?.toLongOrNull()
    
    var tags by remember { mutableStateOf(existingTags.toMutableMap()) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Tags",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Place: $placeKey",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Existing Tags",
            style = MaterialTheme.typography.titleMedium
        )

        tags.forEach { (key, value) ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleSmall
                        )
                        OutlinedTextField(
                            value = value,
                            onValueChange = { newVal -> 
                                tags = tags.toMutableMap().apply { this[key] = newVal }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    IconButton(
                        onClick = { 
                            tags = tags.toMutableMap().apply { remove(key) }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete tag")
                    }
                }
            }
        }

        HorizontalDivider()

        Text(
            text = "Add New Tag",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = newKey,
            onValueChange = { newKey = it },
            label = { Text("Key") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = newValue,
            onValueChange = { newValue = it },
            label = { Text("Value") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = {
                if (newKey.isNotBlank() && newValue.isNotBlank()) {
                    tags = tags.toMutableMap().apply { 
                        this[newKey] = newValue 
                    }
                    newKey = ""
                    newValue = ""
                }
            },
            enabled = newKey.isNotBlank() && newValue.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Tag")
        }

        Text(
            text = "Note: Changes will be synced to OpenStreetMap. Please ensure accuracy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    nodeId?.let {
                        isSaving = true
                        onSave(it, tags)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving && nodeId != null && tags.isNotEmpty()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save to OSM")
                }
            }
        }
    }
}
