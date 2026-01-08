package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun CheckinDialog(
    placeKey: String,
    placeName: String?,
    checkinState: CheckinState,
    onCheckin: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var note by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(checkinState) {
        when (checkinState) {
            is CheckinState.Loading -> isButtonEnabled = false
            is CheckinState.Success -> {
                // 成功を表示するためにダイアログを少し開いたままにしてから閉じる
                kotlinx.coroutines.delay(1000)
                onDismiss()
            }
            is CheckinState.Error -> isButtonEnabled = true
            is CheckinState.Idle -> isButtonEnabled = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.checkin)) },
        modifier = modifier,
        text = {
            Column {
                Text(stringResource(R.string.place_label, placeName ?: placeKey))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                
                when (checkinState) {
                    is CheckinState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is CheckinState.Success -> {
                        Text(
                            stringResource(R.string.checkin_success),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is CheckinState.Error -> {
                        val errorMessage = if (checkinState.message == "duplicate_checkin") {
                            stringResource(R.string.duplicate_checkin)
                        } else {
                            checkinState.message
                        }
                        Text(
                            errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is CheckinState.Idle -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCheckin(note) },
                enabled = isButtonEnabled
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.checkin))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
