package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.Checkin
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    checkins: List<Checkin>,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.checkin_history),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))

        if (checkins.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No check-ins yet")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(checkins) { checkin ->
                    CheckinCard(
                        checkin = checkin,
                        onDeleteClick = { onDeleteClick(checkin.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CheckinCard(
    checkin: Checkin,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val dateText = dateFormat.format(Date(checkin.visitedAt))

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = checkin.place?.name ?: checkin.placeKey,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (checkin.note.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = checkin.note,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_tag),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
