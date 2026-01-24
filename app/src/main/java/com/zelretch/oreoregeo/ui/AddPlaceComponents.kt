package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R

@Composable
@Suppress("FunctionNaming")
fun AddPlaceContent(
    state: AddPlaceState,
    currentLat: Double?,
    currentLon: Double?,
    onSave: (Double, Double, Map<String, String>) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .alpha(if (state.isMapReady) 1f else 0f)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        MapSection(state, currentLat, currentLon)

        OutlinedTextField(
            value = state.name,
            onValueChange = { state.name = it },
            label = { Text(stringResource(R.string.name_required)) },
            modifier = Modifier.fillMaxWidth()
        )

        CoordinateFields(state)

        CategorySection(state)

        OutlinedTextField(
            value = state.additionalTags,
            onValueChange = { state.additionalTags = it },
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

        FormActionButtons(state, onSave, onCancel)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
@Suppress("FunctionNaming")
private fun CoordinateFields(state: AddPlaceState) {
    OutlinedTextField(
        value = state.lat,
        onValueChange = { state.lat = it },
        label = { Text(stringResource(R.string.latitude_required)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = state.lon,
        onValueChange = { state.lon = it },
        label = { Text(stringResource(R.string.longitude_required)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
@Suppress("FunctionNaming")
private fun FormActionButtons(
    state: AddPlaceState,
    onSave: (Double, Double, Map<String, String>) -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .testTag("cancelButton")
        ) {
            Text(stringResource(R.string.cancel))
        }

        Button(
            onClick = {
                val latVal = state.lat.toDoubleOrNull()
                val lonVal = state.lon.toDoubleOrNull()
                if (latVal != null && lonVal != null) {
                    state.isSaving = true
                    onSave(latVal, lonVal, state.createTagsMap())
                }
            },
            modifier = Modifier
                .weight(1f)
                .testTag("saveButton"),
            enabled = !state.isSaving && state.isInputValid()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save_to_osm))
            }
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun MapSection(state: AddPlaceState, currentLat: Double?, currentLon: Double?) {
    val hasCoordinates = state.lat.isNotBlank() && state.lon.isNotBlank()
    val hasCurrentLocation = currentLat != null && currentLon != null
    if (hasCoordinates || hasCurrentLocation) {
        val initialLat = state.lat.toDoubleOrNull() ?: currentLat ?: 0.0
        val initialLon = state.lon.toDoubleOrNull() ?: currentLon ?: 0.0
        val pickedLat = state.lat.toDoubleOrNull()
        val pickedLon = state.lon.toDoubleOrNull()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            MapPickerView(
                initial = Pair(initialLat, initialLon),
                currentLocation = if (currentLat != null && currentLon != null) {
                    Pair(currentLat, currentLon)
                } else {
                    null
                },
                selected = if (pickedLat != null && pickedLon != null) {
                    Pair(pickedLat, pickedLon)
                } else {
                    null
                },
                onPicked = { pLat: Double, pLon: Double ->
                    state.lat = pLat.toString()
                    state.lon = pLon.toString()
                },
                onReady = { state.isMapReady = true },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("mapPicker")
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.select_location_on_map_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@Suppress("FunctionNaming")
private fun CategorySection(state: AddPlaceState) {
    Text(stringResource(R.string.category), style = MaterialTheme.typography.titleSmall)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("amenity", "shop", "tourism").forEach { cat ->
            FilterChip(
                selected = state.category == cat,
                onClick = {
                    state.category = cat
                    state.categoryValue = ""
                },
                label = {
                    val labelId = when (cat) {
                        "amenity" -> R.string.amenity
                        "shop" -> R.string.shop
                        "tourism" -> R.string.tourism
                        else -> 0
                    }
                    Text(stringResource(labelId))
                }
            )
        }
    }

    CategoryValueField(
        category = state.category,
        value = state.categoryValue,
        onValueChange = { state.categoryValue = it }
    )
}
