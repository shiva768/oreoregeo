package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zelretch.oreoregeo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("FunctionNaming", "LongParameterList")
fun AddPlaceScreen(
    currentLat: Double?,
    currentLon: Double?,
    onSave: (Double, Double, Map<String, String>) -> Unit,
    onCancel: () -> Unit,
    onResetEditState: () -> Unit = {},
    modifier: Modifier = Modifier,
    editState: OsmEditState = OsmEditState.Idle
) {
    val state = remember { AddPlaceState(currentLat, currentLon, editState) }

    // エラー時に保存中フラグをリセットする
    LaunchedEffect(editState) {
        if (editState is OsmEditState.Error) {
            state.isSaving = false
        }
    }

    if (editState is OsmEditState.ConfirmDuplicate) {
        state.nearbyPlaces = editState.nearbyPlaces
        state.showDuplicateDialog = true
    }

    if (state.showDuplicateDialog) {
        DuplicateConfirmationDialog(
            state = state,
            onSave = onSave,
            onResetEditState = onResetEditState
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_new_place)) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            AddPlaceContent(
                state = state,
                currentLat = currentLat,
                currentLon = currentLon,
                onSave = onSave,
                onCancel = onCancel
            )

            if (!state.isMapReady) {
                CircularProgressIndicator()
            }
        }
    }
}
