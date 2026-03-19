package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.PlaceWithDistance

@Composable
@Suppress("FunctionNaming")
fun DuplicateConfirmationDialog(
    state: AddPlaceState,
    onSave: (Double, Double, Map<String, String>) -> Unit,
    onResetEditState: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            state.showDuplicateDialog = false
            state.isSaving = false
            onResetEditState()
        },
        title = {
            Text(
                text = if (state.nearbyPlaces.isNotEmpty()) {
                    stringResource(R.string.confirm_duplicate_title)
                } else {
                    stringResource(R.string.confirm_save_title)
                }
            )
        },
        text = {
            DuplicateDialogContent(state.nearbyPlaces)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    state.showDuplicateDialog = false
                    onResetEditState()
                    val latVal = state.lat.toDoubleOrNull()
                    val lonVal = state.lon.toDoubleOrNull()
                    if (latVal != null && lonVal != null) {
                        onSave(latVal, lonVal, state.createTagsMap())
                    }
                }
            ) {
                Text(stringResource(R.string.confirm_save))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                state.showDuplicateDialog = false
                state.isSaving = false
                onResetEditState()
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
@Suppress("FunctionNaming")
private fun DuplicateDialogContent(nearbyPlaces: List<PlaceWithDistance>) {
    Column {
        if (nearbyPlaces.isNotEmpty()) {
            Text(stringResource(R.string.confirm_duplicate_message))
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.nearby_places_list),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.height(200.dp)) {
                items(nearbyPlaces) { placeWithDistance ->
                    NearbyPlaceItem(placeWithDistance)
                }
            }
        } else {
            Text(stringResource(R.string.confirm_save_message))
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun NearbyPlaceItem(placeWithDistance: PlaceWithDistance) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = placeWithDistance.place.name,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${placeWithDistance.place.category} " +
                "(${placeWithDistance.distanceMeters.toInt()}m)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}
