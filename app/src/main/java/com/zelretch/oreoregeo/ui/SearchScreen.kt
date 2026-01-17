package com.zelretch.oreoregeo.ui

import android.graphics.Color
import android.os.Build
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zelretch.oreoregeo.BuildConfig
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.PlaceWithDistance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.text.DecimalFormat

@Composable
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
            modifier = Modifier.fillMaxWidth(),
            enabled = searchState !is SearchState.Loading
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
            }
            is SearchState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // テスト要件に合わせ、エラーメッセージは装飾せずそのまま表示する
                        Text(state.message, color = MaterialTheme.colorScheme.error)
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

    // 半径に基づいた適切なズームレベルを計算
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
            if (BuildConfig.IS_CI) {
                // CI 上の UI テストでは MapView を生成せず空のコンテナを返す
                return@AndroidView FrameLayout(ctx)
            }
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                if (isRunningOnEmulator()) {
                    this.setUseDataConnection(false)
                    try {
                        this.onPause()
                    } catch (_: Exception) {
                        // no-op
                    }
                }
                controller.setZoom(targetZoom)
                controller.setCenter(GeoPoint(location.first, location.second))

                // 現在地のマーカーを追加
                val marker = Marker(this)
                marker.position = GeoPoint(location.first, location.second)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.icon = context.getDrawable(R.drawable.ic_current_location)
                marker.title = context.getString(R.string.current_location)
                overlays.add(marker)

                // 検索半径の円を追加
                val circle = Polygon.pointsAsCircle(GeoPoint(location.first, location.second), radiusMeters.toDouble())
                val circleOverlay = Polygon(this)
                circleOverlay.points = circle
                circleOverlay.fillPaint.color = Color.argb(50, 0, 0, 255)
                circleOverlay.outlinePaint.color = Color.BLUE
                circleOverlay.outlinePaint.strokeWidth = 2f
                overlays.add(circleOverlay)
            }
        },
        update = { view ->
            val mapView = view as? MapView ?: return@AndroidView
            mapView.overlays.clear()

            // スポットが選択されていない場合、半径に基づいてズームを更新
            if (selectedPlaceLocation == null) {
                mapView.controller.setZoom(targetZoom)
            }

            // 現在地のマーカー
            val marker = Marker(mapView)
            marker.position = GeoPoint(location.first, location.second)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            marker.icon = context.getDrawable(R.drawable.ic_current_location)
            marker.title = context.getString(R.string.current_location)
            mapView.overlays.add(marker)

            // 検索半径の円
            val circle = Polygon.pointsAsCircle(GeoPoint(location.first, location.second), radiusMeters.toDouble())
            val circleOverlay = Polygon(mapView)
            circleOverlay.points = circle
            circleOverlay.fillPaint.color = Color.argb(50, 0, 0, 255)
            circleOverlay.outlinePaint.color = Color.BLUE
            circleOverlay.outlinePaint.strokeWidth = 2f
            mapView.overlays.add(circleOverlay)

            // 選択された場所のマーカー
            selectedPlaceLocation?.let {
                val selectedMarker = Marker(mapView)
                selectedMarker.position = GeoPoint(it.first, it.second)
                selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                selectedMarker.icon = context.getDrawable(R.drawable.ic_selected_place)
                selectedMarker.title = context.getString(R.string.selected_place)
                mapView.overlays.add(selectedMarker)

                // 選択された場所にアニメーションで移動
                mapView.controller.animateTo(GeoPoint(it.first, it.second))
            } ?: run {
                // 何も選択されていない場合、現在地を中心に表示
                mapView.controller.setCenter(GeoPoint(location.first, location.second))
            }

            mapView.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun isRunningOnEmulator(): Boolean {
    val fingerprint = Build.FINGERPRINT
    val model = Build.MODEL
    val manufacturer = Build.MANUFACTURER
    val brand = Build.BRAND
    val device = Build.DEVICE
    val product = Build.PRODUCT

    return fingerprint.startsWith("generic") ||
        fingerprint.startsWith("unknown") ||
        model.contains("google_sdk", ignoreCase = true) ||
        model.contains("Emulator", ignoreCase = true) ||
        model.contains("Android SDK built for x86", ignoreCase = true) ||
        manufacturer.contains("Genymotion", ignoreCase = true) ||
        (brand.startsWith("generic") && device.startsWith("generic")) ||
        product == "google_sdk"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCard(
    placeWithDistance: PlaceWithDistance,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onCheckinClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
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
