package com.zelretch.oreoregeo.ui

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
import androidx.compose.ui.res.stringResource
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.PlaceWithDistance
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import android.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import java.text.DecimalFormat

@Composable
fun SearchScreen(
    searchState: SearchState,
    searchRadius: Int,
    onRadiusChange: (Int) -> Unit,
    excludeUnnamed: Boolean,
    onExcludeUnnamedChange: (Boolean) -> Unit,
    canEdit: Boolean,
    currentLocation: Pair<Double, Double>? = null,
    onSearchClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onCheckinClick: (String) -> Unit,
    onEditPlace: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedPlaceLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedPlaceKey by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (currentLocation != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                MapViewContainer(
                    location = currentLocation,
                    radiusMeters = searchRadius,
                    selectedPlaceLocation = selectedPlaceLocation
                )
            }
            Spacer(Modifier.height(16.dp))
        }

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
                    modifier = Modifier.scale(0.7f)
                )
            }
        }
        
        Slider(
            value = searchRadius.toFloat(),
            onValueChange = { onRadiusChange(it.toInt()) },
            valueRange = 50f..500f,
            steps = 9, // 50, 100, 150, ..., 500
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onSearchClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.search_nearby_places))
        }

        Spacer(Modifier.height(16.dp))

        when (val state = searchState) {
            is SearchState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.tap_to_search))
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
                        Text(stringResource(R.string.no_places_found))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.places) { placeWithDistance ->
                            val isSelected = selectedPlaceKey == placeWithDistance.place.placeKey
                            PlaceCard(
                                placeWithDistance = placeWithDistance,
                                isSelected = isSelected,
                                onClick = { 
                                    selectedPlaceLocation = placeWithDistance.place.lat to placeWithDistance.place.lon
                                    selectedPlaceKey = placeWithDistance.place.placeKey
                                    onPlaceClick(placeWithDistance.place.placeKey) 
                                },
                                onCheckinClick = {
                                    onCheckinClick(placeWithDistance.place.placeKey)
                                },
                                onEditClick = if (canEdit && placeWithDistance.place.placeKey.contains(":node:") && onEditPlace != null) {
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
                        Text(stringResource(R.string.error_msg, state.message), color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onSearchClick) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapViewContainer(
    location: Pair<Double, Double>,
    radiusMeters: Int,
    selectedPlaceLocation: Pair<Double, Double>? = null
) {
    val context = LocalContext.current
    
    // Calculate appropriate zoom level based on radius
    // 50m -> ~18.5, 500m -> ~15.5
    val targetZoom = when {
        radiusMeters <= 50 -> 18.5
        radiusMeters <= 100 -> 18.0
        radiusMeters <= 200 -> 17.0
        radiusMeters <= 300 -> 16.5
        radiusMeters <= 400 -> 16.0
        else -> 15.5
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(targetZoom)
                controller.setCenter(GeoPoint(location.first, location.second))
                
                // Add marker for current location
                val marker = Marker(this)
                marker.position = GeoPoint(location.first, location.second)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.icon = context.getDrawable(R.drawable.ic_current_location)
                marker.title = context.getString(R.string.current_location)
                overlays.add(marker)
                
                // Add radius circle
                val circle = Polygon.pointsAsCircle(GeoPoint(location.first, location.second), radiusMeters.toDouble())
                val circleOverlay = Polygon(this)
                circleOverlay.points = circle
                circleOverlay.fillPaint.color = Color.argb(50, 0, 0, 255)
                circleOverlay.outlinePaint.color = Color.BLUE
                circleOverlay.outlinePaint.strokeWidth = 2f
                overlays.add(circleOverlay)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            
            // Update zoom based on radius if no place is selected
            if (selectedPlaceLocation == null) {
                mapView.controller.setZoom(targetZoom)
            }
            
            // Current location marker
            val marker = Marker(mapView)
            marker.position = GeoPoint(location.first, location.second)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            marker.icon = context.getDrawable(R.drawable.ic_current_location)
            marker.title = context.getString(R.string.current_location)
            mapView.overlays.add(marker)
            
            // Search radius circle
            val circle = Polygon.pointsAsCircle(GeoPoint(location.first, location.second), radiusMeters.toDouble())
            val circleOverlay = Polygon(mapView)
            circleOverlay.points = circle
            circleOverlay.fillPaint.color = Color.argb(50, 0, 0, 255)
            circleOverlay.outlinePaint.color = Color.BLUE
            circleOverlay.outlinePaint.strokeWidth = 2f
            mapView.overlays.add(circleOverlay)

            // Selected place marker
            selectedPlaceLocation?.let {
                val selectedMarker = Marker(mapView)
                selectedMarker.position = GeoPoint(it.first, it.second)
                selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                selectedMarker.icon = context.getDrawable(R.drawable.ic_selected_place)
                selectedMarker.title = context.getString(R.string.selected_place)
                mapView.overlays.add(selectedMarker)
                
                // Animate to selected place
                mapView.controller.animateTo(GeoPoint(it.first, it.second))
            } ?: run {
                // If nothing selected, center on current location
                mapView.controller.setCenter(GeoPoint(location.first, location.second))
            }
            
            mapView.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCard(
    placeWithDistance: PlaceWithDistance,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCheckinClick: () -> Unit,
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
        modifier = modifier.fillMaxWidth(),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
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
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Button(
                        onClick = onCheckinClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(stringResource(R.string.checkin))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                
                if (onEditClick != null) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_tags_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
