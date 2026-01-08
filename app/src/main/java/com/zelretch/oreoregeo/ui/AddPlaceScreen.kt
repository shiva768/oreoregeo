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
import androidx.compose.ui.res.stringResource
import com.zelretch.oreoregeo.R
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
            text = stringResource(R.string.add_new_place),
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name_required)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lat,
            onValueChange = { lat = it },
            label = { Text(stringResource(R.string.latitude_required)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lon,
            onValueChange = { lon = it },
            label = { Text(stringResource(R.string.longitude_required)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.category),
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = category == "amenity",
                onClick = { category = "amenity" },
                label = { Text(stringResource(R.string.amenity)) }
            )
            FilterChip(
                selected = category == "shop",
                onClick = { category = "shop" },
                label = { Text(stringResource(R.string.shop)) }
            )
            FilterChip(
                selected = category == "tourism",
                onClick = { category = "tourism" },
                label = { Text(stringResource(R.string.tourism)) }
            )
        }

        OutlinedTextField(
            value = categoryValue,
            onValueChange = { categoryValue = it },
            label = { Text(stringResource(R.string.category_value_label, category)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = additionalTags,
            onValueChange = { additionalTags = it },
            label = { Text(stringResource(R.string.additional_tags_label)) },
            placeholder = { Text(stringResource(R.string.additional_tags_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Text(
            text = stringResource(R.string.osm_accuracy_notice),
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
                Text(stringResource(R.string.cancel))
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
                        
                        // 追加タグをパース
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
                    Text(stringResource(R.string.save_to_osm))
                }
            }
        }
    }
}
