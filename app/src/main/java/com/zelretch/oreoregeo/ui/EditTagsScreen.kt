package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R

@Composable
@Suppress("FunctionNaming")
fun EditTagsScreen(
    placeKey: String,
    existingTags: Map<String, String>,
    onSave: (Long, Map<String, String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // place_key (osm:node:12345) からノードIDを抽出
    val nodeId = placeKey.split(":").lastOrNull()?.toLongOrNull()

    var tags by remember { mutableStateOf(existingTags) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Update tags when existingTags changes (e.g., when loaded from OSM)
    LaunchedEffect(existingTags) {
        if (existingTags.isNotEmpty()) {
            tags = existingTags
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EditTagsHeader(placeKey)

        ExistingTagsSection(
            tags = tags,
            onTagChange = { key, newVal ->
                tags = tags.toMutableMap().apply { this[key] = newVal }
            },
            onTagDelete = { key ->
                tags = tags.toMutableMap().apply { remove(key) }
            }
        )

        HorizontalDivider()

        AddNewTagSection(
            newKey = newKey,
            onKeyChange = { newKey = it },
            newValue = newValue,
            onValueChange = { newValue = it },
            onAddClick = {
                tags = tags.toMutableMap().apply { this[newKey] = newValue }
                newKey = ""
                newValue = ""
            }
        )

        Text(
            text = stringResource(R.string.osm_accuracy_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        EditTagsActions(
            isSaving = isSaving,
            canSave = nodeId != null && tags.isNotEmpty(),
            onCancel = onCancel,
            onSave = {
                nodeId?.let {
                    isSaving = true
                    onSave(it, tags)
                }
            }
        )
    }
}

@Composable
@Suppress("FunctionNaming")
private fun EditTagsHeader(placeKey: String) {
    Text(
        text = stringResource(R.string.edit_tags_title),
        style = MaterialTheme.typography.headlineMedium
    )

    Text(
        text = stringResource(R.string.place_label, placeKey),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
@Suppress("FunctionNaming")
private fun ExistingTagsSection(
    tags: Map<String, String>,
    onTagChange: (String, String) -> Unit,
    onTagDelete: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.existing_tags),
        style = MaterialTheme.typography.titleMedium
    )

    tags.forEach { (key, value) ->
        TagEditItem(key, value, onTagChange, onTagDelete)
    }
}

@Composable
@Suppress("FunctionNaming")
private fun TagEditItem(
    key: String,
    value: String,
    onTagChange: (String, String) -> Unit,
    onTagDelete: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = key, style = MaterialTheme.typography.titleSmall)
                if (key == "amenity" || key == "shop" || key == "tourism") {
                    CategoryValueField(
                        category = key,
                        value = value,
                        onValueChange = { onTagChange(key, it) }
                    )
                } else {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { onTagChange(key, it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            IconButton(onClick = { onTagDelete(key) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_tag)
                )
            }
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun AddNewTagSection(
    newKey: String,
    onKeyChange: (String) -> Unit,
    newValue: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.add_new_tag),
        style = MaterialTheme.typography.titleMedium
    )

    OutlinedTextField(
        value = newKey,
        onValueChange = onKeyChange,
        label = { Text(stringResource(R.string.key_label)) },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = newValue,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.value_label)) },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedButton(
        onClick = onAddClick,
        enabled = newKey.isNotBlank() && newValue.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.add_tag))
    }
}

@Composable
@Suppress("FunctionNaming")
private fun EditTagsActions(
    isSaving: Boolean,
    canSave: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.cancel))
        }

        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            enabled = !isSaving && canSave
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save_to_osm))
            }
        }
    }
}
