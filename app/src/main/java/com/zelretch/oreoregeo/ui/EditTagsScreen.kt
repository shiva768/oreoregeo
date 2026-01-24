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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R

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
                        if (key == "amenity" || key == "shop" || key == "tourism") {
                            CategoryValueField(
                                category = key,
                                value = value,
                                onValueChange = { newVal: String ->
                                    tags = tags.toMutableMap().apply { this[key] = newVal }
                                }
                            )
                        } else {
                            OutlinedTextField(
                                value = value,
                                onValueChange = { newVal ->
                                    tags = tags.toMutableMap().apply { this[key] = newVal }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryValueField(
    category: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = when (category) {
        "amenity" -> listOf(
            stringResource(R.string.amenity_restaurant),
            stringResource(R.string.amenity_cafe),
            stringResource(R.string.amenity_fast_food),
            stringResource(R.string.amenity_bar),
            stringResource(R.string.amenity_pub),
            stringResource(R.string.amenity_convenience),
            stringResource(R.string.amenity_vending_machine),
            stringResource(R.string.amenity_parking),
            stringResource(R.string.amenity_bench),
            stringResource(R.string.amenity_toilets),
            stringResource(R.string.amenity_post_box)
        )
        "shop" -> listOf(
            stringResource(R.string.shop_convenience),
            stringResource(R.string.shop_supermarket),
            stringResource(R.string.shop_clothes),
            stringResource(R.string.shop_hairdresser),
            stringResource(R.string.shop_bakery),
            stringResource(R.string.shop_drugstore)
        )
        "tourism" -> listOf(
            stringResource(R.string.tourism_information),
            stringResource(R.string.tourism_attraction),
            stringResource(R.string.tourism_viewpoint),
            stringResource(R.string.tourism_hotel),
            stringResource(R.string.tourism_museum),
            stringResource(R.string.tourism_artwork)
        )
        else -> emptyList()
    }

    val filteredOptions by remember(value, options) {
        derivedStateOf {
            if (value.isEmpty()) {
                options
            } else {
                options.filter { it.contains(value, ignoreCase = true) }
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.category_value_label, category)) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                .testTag("categoryValueField"),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onValueChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
