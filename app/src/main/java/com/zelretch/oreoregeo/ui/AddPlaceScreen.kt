package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    currentLat: Double?,
    currentLon: Double?,
    onSave: (Double, Double, Map<String, String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var lat by remember { mutableStateOf(currentLat?.toString() ?: "") }
    var lon by remember { mutableStateOf(currentLon?.toString() ?: "") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("amenity") }
    var categoryValue by remember { mutableStateOf("") }
    var additionalTags by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add New Place",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lat,
            onValueChange = { lat = it },
            label = { Text("Latitude *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lon,
            onValueChange = { lon = it },
            label = { Text("Longitude *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Category",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = category == "amenity",
                onClick = { category = "amenity" },
                label = { Text("Amenity") }
            )
            FilterChip(
                selected = category == "shop",
                onClick = { category = "shop" },
                label = { Text("Shop") }
            )
            FilterChip(
                selected = category == "tourism",
                onClick = { category = "tourism" },
                label = { Text("Tourism") }
            )
        }

        OutlinedTextField(
            value = categoryValue,
            onValueChange = { categoryValue = it },
            label = { Text("$category value (e.g., restaurant, cafe) *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = additionalTags,
            onValueChange = { additionalTags = it },
            label = { Text("Additional tags (optional)") },
            placeholder = { Text("key1=value1, key2=value2") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

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
                    val latValue = lat.toDoubleOrNull()
                    val lonValue = lon.toDoubleOrNull()
                    if (latValue != null && lonValue != null && name.isNotBlank() && categoryValue.isNotBlank()) {
                        val tags = mutableMapOf(
                            "name" to name,
                            category to categoryValue
                        )
                        
                        // Parse additional tags
                        if (additionalTags.isNotBlank()) {
                            additionalTags.split(",").forEach { tagPair ->
                                val parts = tagPair.trim().split("=")
                                if (parts.size == 2) {
                                    tags[parts[0].trim()] = parts[1].trim()
                                }
                            }
                        }
                        
                        isSaving = true
                        onSave(latValue, lonValue, tags)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving && lat.toDoubleOrNull() != null && 
                         lon.toDoubleOrNull() != null && 
                         name.isNotBlank() && 
                         categoryValue.isNotBlank()
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
