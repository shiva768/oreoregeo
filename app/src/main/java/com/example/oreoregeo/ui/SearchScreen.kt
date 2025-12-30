package com.example.oreoregeo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oreoregeo.domain.PlaceWithDistance
import java.text.DecimalFormat

@Composable
fun SearchScreen(
    searchState: SearchState,
    onSearchClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onEditPlace: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onSearchClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Search Nearby Places")
        }

        Spacer(Modifier.height(16.dp))

        when (val state = searchState) {
            is SearchState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tap the button to search for nearby places")
                }
            }
            is SearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SearchState.Success -> {
                if (state.places.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No places found nearby")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.places) { placeWithDistance ->
                            PlaceCard(
                                placeWithDistance = placeWithDistance,
                                onClick = { onPlaceClick(placeWithDistance.place.placeKey) },
                                onEditClick = if (placeWithDistance.place.placeKey.contains(":node:") && onEditPlace != null) {
                                    { onEditPlace(placeWithDistance.place.placeKey) }
                                } else null
                            )
                        }
                    }
                }
            }
            is SearchState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onSearchClick) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceCard(
    placeWithDistance: PlaceWithDistance,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val place = placeWithDistance.place
    val distanceMeters = placeWithDistance.distanceMeters
    val df = DecimalFormat("#.#")
    val distanceText = if (distanceMeters < 1000) {
        "${df.format(distanceMeters)}m"
    } else {
        "${df.format(distanceMeters / 1000)}km"
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = place.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = place.placeKey,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onEditClick != null) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit tags",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
