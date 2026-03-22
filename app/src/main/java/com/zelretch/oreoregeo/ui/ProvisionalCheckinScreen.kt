package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.ProvisionalCheckin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@Suppress("FunctionNaming", "LongMethod")
fun ProvisionalCheckinScreen(
    pendingCheckins: List<ProvisionalCheckin>,
    confirmState: ProvisionalCheckinConfirmState,
    onConfirm: (provisionalId: Long, placeKey: String, note: String) -> Unit,
    onDismiss: (id: Long) -> Unit,
    onConfirmStateReset: () -> Unit,
    onLoadGeocode: (lat: Double, lon: Double) -> Unit = { _, _ -> },
    onClearGeocode: () -> Unit = {},
    geocodePrefName: String? = null,
    geocodeCityName: String? = null
) {
    var selectedCheckin by remember { mutableStateOf<ProvisionalCheckin?>(null) }
    var mapCheckin by remember { mutableStateOf<ProvisionalCheckin?>(null) }

    LaunchedEffect(confirmState) {
        if (confirmState is ProvisionalCheckinConfirmState.Success) {
            selectedCheckin = null
            onConfirmStateReset()
        }
    }

    if (pendingCheckins.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.no_provisional_checkins),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pendingCheckins, key = { it.id }) { checkin ->
            ProvisionalCheckinCard(
                checkin = checkin,
                onConfirmClick = { selectedCheckin = checkin },
                onDismissClick = { onDismiss(checkin.id) },
                onMapClick = {
                    mapCheckin = checkin
                    onLoadGeocode(checkin.lat, checkin.lon)
                }
            )
        }
    }

    mapCheckin?.let { checkin ->
        LocationMapDialog(
            lat = checkin.lat,
            lon = checkin.lon,
            name = checkin.placeName ?: stringResource(R.string.unknown_place),
            prefName = geocodePrefName,
            cityName = geocodeCityName,
            onDismiss = {
                mapCheckin = null
                onClearGeocode()
            }
        )
    }

    selectedCheckin?.let { checkin ->
        ProvisionalCheckinConfirmDialog(
            checkin = checkin,
            confirmState = confirmState,
            onConfirm = { placeKey, note -> onConfirm(checkin.id, placeKey, note) },
            onDismiss = {
                selectedCheckin = null
                onConfirmStateReset()
            }
        )
    }
}

@Composable
@Suppress("FunctionNaming")
private fun ProvisionalCheckinCard(
    checkin: ProvisionalCheckin,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    onMapClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = checkin.placeName ?: stringResource(R.string.unknown_place),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.provisional_detected_at,
                    dateFormat.format(Date(checkin.detectedAt))
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onMapClick) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = stringResource(R.string.show_map),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    TextButton(onClick = onDismissClick) {
                        Text(stringResource(R.string.provisional_dismiss))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirmClick) {
                        Text(stringResource(R.string.provisional_confirm))
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("FunctionNaming", "LongMethod")
private fun ProvisionalCheckinConfirmDialog(
    checkin: ProvisionalCheckin,
    confirmState: ProvisionalCheckinConfirmState,
    onConfirm: (placeKey: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf("") }
    val isLoading = confirmState is ProvisionalCheckinConfirmState.Loading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(R.string.provisional_confirm_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(
                        R.string.provisional_confirm_message,
                        checkin.placeName ?: stringResource(R.string.unknown_place)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                if (confirmState is ProvisionalCheckinConfirmState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.error_msg, confirmState.message),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(checkin.placeKey, note) },
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.checkin))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
