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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.Checkin
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    checkins: List<Checkin>,
    placeNameQuery: String,
    startDate: Long?,
    endDate: Long?,
    onPlaceNameQueryChange: (String) -> Unit,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onClearFilters: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }
    val hasActiveFilters = placeNameQuery.isNotEmpty() || 
        startDate != null || endDate != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search button/indicator
        if (!showFilters && !hasActiveFilters) {
            // Show search button when filters are hidden and no active filters
            TextButton(
                onClick = { showFilters = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(stringResource(R.string.search))
            }
        } else if (!showFilters && hasActiveFilters) {
            // Show active filters indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showFilters = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(stringResource(R.string.filter_active))
                }
                IconButton(onClick = onClearFilters) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_filters)
                    )
                }
            }
        }

        // Search filters (collapsible)
        if (showFilters) {
            SearchFilters(
                placeNameQuery = placeNameQuery,
                startDate = startDate,
                endDate = endDate,
                onPlaceNameQueryChange = onPlaceNameQueryChange,
                onStartDateChange = onStartDateChange,
                onEndDateChange = onEndDateChange,
                onClearFilters = onClearFilters,
                onHideFilters = { showFilters = false }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (checkins.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_checkins_yet))
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
fun SearchFilters(
    placeNameQuery: String,
    startDate: Long?,
    endDate: Long?,
    onPlaceNameQueryChange: (String) -> Unit,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onClearFilters: () -> Unit,
    onHideFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.search_filters),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onHideFilters) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close)
                )
            }
        }

        // Place name filter
        OutlinedTextField(
            value = placeNameQuery,
            onValueChange = onPlaceNameQueryChange,
            label = { Text(stringResource(R.string.filter_place_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (placeNameQuery.isNotEmpty()) {
                    IconButton(onClick = { onPlaceNameQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            }
        )

        // Date range filters in a compact row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start date with calendar icon
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = startDate?.let { dateFormat.format(Date(it)) } ?: "",
                    onValueChange = { },
                    label = { Text(stringResource(R.string.filter_start_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            if (startDate != null) {
                                IconButton(onClick = { onStartDateChange(null) }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = stringResource(R.string.select_start_date)
                                )
                            }
                        }
                    },
                    placeholder = { Text(stringResource(R.string.select_date)) }
                )
            }

            // End date with calendar icon
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = endDate?.let { dateFormat.format(Date(it)) } ?: "",
                    onValueChange = { },
                    label = { Text(stringResource(R.string.filter_end_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            if (endDate != null) {
                                IconButton(onClick = { onEndDateChange(null) }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = stringResource(R.string.select_end_date)
                                )
                            }
                        }
                    },
                    placeholder = { Text(stringResource(R.string.select_date)) }
                )
            }
        }

        // Clear filters button
        val hasActiveFilters = placeNameQuery.isNotEmpty() || 
            startDate != null || endDate != null
        
        if (hasActiveFilters) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(stringResource(R.string.clear_filters))
            }
        }
    }

    // Date picker dialogs
    if (showStartDatePicker) {
        MaterialDatePickerDialog(
            initialDate = startDate,
            onDateSelected = { date ->
                onStartDateChange(date)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        MaterialDatePickerDialog(
            initialDate = endDate,
            onDateSelected = { date ->
                onEndDateChange(date)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

private fun normalizeToStartOfDay(millis: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MaterialDatePickerDialog(
    initialDate: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDate ?: System.currentTimeMillis()
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(normalizeToStartOfDay(millis))
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        androidx.compose.material3.DatePicker(
            state = datePickerState
        )
    }
}

@Composable
fun CheckinCard(checkin: Checkin, onDeleteClick: () -> Unit, modifier: Modifier = Modifier) {
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
