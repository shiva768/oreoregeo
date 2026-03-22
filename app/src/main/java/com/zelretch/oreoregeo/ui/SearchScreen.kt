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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.PlaceWithDistance

private const val SEARCH_RADIUS_RANGE_START = 50f
private const val SEARCH_RADIUS_RANGE_END = 500f
private const val SEARCH_RADIUS_STEPS = 9
private const val SWITCH_SCALE = 0.7f

@Composable
@Suppress("FunctionNaming", "LongParameterList")
fun SearchScreen(
    searchState: SearchState,
    searchRadius: Int,
    onRadiusChange: (Int) -> Unit,
    excludeUnnamed: Boolean,
    onExcludeUnnamedChange: (Boolean) -> Unit,
    canEdit: Boolean,
    modifier: Modifier = Modifier,
    currentLocation: Pair<Double, Double>? = null,
    onSearchClick: () -> Unit,
    onCancelClick: () -> Unit = {},
    onPlaceClick: (String) -> Unit,
    onCheckinClick: (String) -> Unit,
    onEditPlace: ((String) -> Unit)? = null
) {
    var selectedPlaceLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedPlaceKey by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchHeader(currentLocation, searchRadius, selectedPlaceLocation)

        SearchFiltersSection(
            searchRadius = searchRadius,
            onRadiusChange = onRadiusChange,
            excludeUnnamed = excludeUnnamed,
            onExcludeUnnamedChange = onExcludeUnnamedChange
        )

        Spacer(Modifier.height(8.dp))

        SearchButton(searchState, onSearchClick, onCancelClick)

        Spacer(Modifier.height(16.dp))

        SearchResultsSection(
            searchState = searchState,
            canEdit = canEdit,
            selectedPlaceKey = selectedPlaceKey,
            onPlaceSelect = { place ->
                selectedPlaceLocation = place.lat to place.lon
                selectedPlaceKey = place.placeKey
                onPlaceClick(place.placeKey)
            },
            onCheckinClick = onCheckinClick,
            onEditPlace = onEditPlace,
            onRetryClick = onSearchClick
        )
    }
}

@Composable
@Suppress("FunctionNaming")
private fun SearchHeader(
    currentLocation: Pair<Double, Double>?,
    searchRadius: Int,
    selectedPlaceLocation: Pair<Double, Double>?
) {
    if (currentLocation != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .clipToBounds()
        ) {
            SearchMapView(
                location = currentLocation,
                radiusMeters = searchRadius,
                selectedPlaceLocation = selectedPlaceLocation
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
@Suppress("FunctionNaming")
private fun SearchFiltersSection(
    searchRadius: Int,
    onRadiusChange: (Int) -> Unit,
    excludeUnnamed: Boolean,
    onExcludeUnnamedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.search_radius_label, searchRadius),
            style = MaterialTheme.typography.labelLarge
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.exclude_unnamed_label),
                style = MaterialTheme.typography.labelSmall
            )
            Switch(
                checked = excludeUnnamed,
                onCheckedChange = onExcludeUnnamedChange,
                modifier = Modifier.scale(SWITCH_SCALE)
            )
        }
    }

    Slider(
        value = searchRadius.toFloat(),
        onValueChange = { onRadiusChange(it.toInt()) },
        valueRange = SEARCH_RADIUS_RANGE_START..SEARCH_RADIUS_RANGE_END,
        steps = SEARCH_RADIUS_STEPS,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
@Suppress("FunctionNaming")
private fun SearchButton(searchState: SearchState, onSearchClick: () -> Unit, onCancelClick: () -> Unit) {
    if (searchState is SearchState.Loading) {
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.cancel))
        }
    } else {
        Button(
            onClick = onSearchClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.search_nearby_places))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("FunctionNaming", "LongParameterList")
private fun SearchResultsSection(
    searchState: SearchState,
    canEdit: Boolean,
    selectedPlaceKey: String?,
    onPlaceSelect: (com.zelretch.oreoregeo.domain.Place) -> Unit,
    onCheckinClick: (String) -> Unit,
    onEditPlace: ((String) -> Unit)? = null,
    onRetryClick: () -> Unit = {}
) {
    when (searchState) {
        is SearchState.Idle -> CenteredText(stringResource(R.string.tap_to_search))
        is SearchState.Loading -> LoadingView()
        is SearchState.Success -> {
            if (searchState.places.isEmpty()) {
                CenteredText(stringResource(R.string.no_places_found))
            } else {
                PlacesList(
                    places = searchState.places,
                    canEdit = canEdit,
                    selectedPlaceKey = selectedPlaceKey,
                    onPlaceSelect = onPlaceSelect,
                    onCheckinClick = onCheckinClick,
                    onEditPlace = onEditPlace
                )
            }
        }
        is SearchState.Error -> SearchErrorView(searchState.errorType, onRetryClick)
    }
}

@Composable
@Suppress("FunctionNaming")
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
@Suppress("FunctionNaming")
private fun CenteredText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}

@Composable
@Suppress("FunctionNaming", "LongParameterList")
private fun PlacesList(
    places: List<PlaceWithDistance>,
    canEdit: Boolean,
    selectedPlaceKey: String?,
    onPlaceSelect: (com.zelretch.oreoregeo.domain.Place) -> Unit,
    onCheckinClick: (String) -> Unit,
    onEditPlace: ((String) -> Unit)? = null
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(places) { placeWithDistance ->
            val isSelected = selectedPlaceKey == placeWithDistance.place.placeKey
            PlaceCard(
                placeWithDistance = placeWithDistance,
                isSelected = isSelected,
                onClick = { onPlaceSelect(placeWithDistance.place) },
                onCheckinClick = { onCheckinClick(placeWithDistance.place.placeKey) },
                onEditClick = if (canEdit &&
                    placeWithDistance.place.placeKey.contains(":node:") &&
                    onEditPlace != null
                ) {
                    { onEditPlace(placeWithDistance.place.placeKey) }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun SearchErrorView(errorType: SearchErrorType, onRetryClick: () -> Unit) {
    val message = when (errorType) {
        SearchErrorType.TIMEOUT -> stringResource(R.string.error_network_timeout)
        SearchErrorType.OFFLINE -> stringResource(R.string.error_network_offline)
        SearchErrorType.GENERIC -> stringResource(R.string.error_network_generic)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onRetryClick) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.retry))
            }
        }
    }
}
