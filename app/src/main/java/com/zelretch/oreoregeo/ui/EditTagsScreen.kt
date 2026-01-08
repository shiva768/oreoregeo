package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zelretch.oreoregeo.R
import androidx.compose.ui.unit.dp

@Composable
fun EditTagsScreen(
    placeKey: String,
    existingTags: Map<String, String>,
    onSave: (Long, Map<String, String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // place_key (osm:node:12345) からノードIDを抽出
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
            text = stringResource(R.string.edit_tags_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = stringResource(R.string.place_label, placeKey),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(R.string.existing_tags),
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
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_tag))
                    }
                }
            }
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.add_new_tag),
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = newKey,
            onValueChange = { newKey = it },
            label = { Text(stringResource(R.string.key_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = newValue,
            onValueChange = { newValue = it },
            label = { Text(stringResource(R.string.value_label)) },
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
            Text(stringResource(R.string.add_tag))
        }

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
                    Text(stringResource(R.string.save_to_osm))
                }
            }
        }
    }
}
