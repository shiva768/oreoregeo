package com.zelretch.oreoregeo.ui

import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.PlaceWithDistance
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import timber.log.Timber

private const val TARGET_ZOOM = 17.0
private const val MAP_READY_DELAY_MS = 500L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    currentLat: Double?,
    currentLon: Double?,
    onSave: (Double, Double, Map<String, String>) -> Unit,
    onCancel: () -> Unit,
    onResetEditState: () -> Unit = {},
    modifier: Modifier = Modifier,
    editState: OsmEditState = OsmEditState.Idle
) {
    var lat by remember { mutableStateOf(currentLat?.toString() ?: "") }
    var lon by remember { mutableStateOf(currentLon?.toString() ?: "") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("amenity") }
    var categoryValue by remember { mutableStateOf("") }
    var additionalTags by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isMapReady by remember { mutableStateOf(false) }

    // エラー時に保存中フラグをリセットする
    androidx.compose.runtime.LaunchedEffect(editState) {
        if (editState is OsmEditState.Error) {
            isSaving = false
        }
    }

    // 重複確認ダイアログの表示制御
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var nearbyPlaces by remember { mutableStateOf<List<PlaceWithDistance>>(emptyList()) }

    if (editState is OsmEditState.ConfirmDuplicate) {
        nearbyPlaces = editState.nearbyPlaces
        showDuplicateDialog = true
    }

    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = {
                showDuplicateDialog = false
                isSaving = false
                onResetEditState()
            },
            title = {
                Text(
                    text = if (nearbyPlaces.isNotEmpty()) {
                        stringResource(R.string.confirm_duplicate_title)
                    } else {
                        stringResource(R.string.confirm_save_title)
                    }
                )
            },
            text = {
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
                        LazyColumn(
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(nearbyPlaces) { placeWithDistance ->
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
                        }
                    } else {
                        Text(stringResource(R.string.confirm_save_message))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDuplicateDialog = false
                        // Keep isSaving = true as we are proceeding to save
                        onResetEditState()
                        val latValue = lat.toDoubleOrNull()
                        val lonValue = lon.toDoubleOrNull()
                        if (latValue != null && lonValue != null) {
                            val tags = mutableMapOf(
                                "name" to name,
                                category to categoryValue
                            )
                            // 追加タグをパース
                            if (additionalTags.isNotBlank()) {
                                additionalTags.split(",").forEach { tagPair ->
                                    if (tagPair.contains("=")) {
                                        val parts = tagPair.trim().split("=")
                                        if (parts.size == 2) {
                                            tags[parts[0].trim()] = parts[1].trim()
                                        }
                                    }
                                }
                            }
                            onSave(latValue, lonValue, tags)
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm_save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDuplicateDialog = false
                    isSaving = false
                    onResetEditState()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
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
            // 全体のコンテンツ（マップ準備完了まで透明）
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isMapReady) 1f else 0f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // マップから位置を選べる UI（現在地がある場合に表示）
                val hasCoordinates = lat.isNotBlank() && lon.isNotBlank()
                val hasCurrentLocation = currentLat != null && currentLon != null
                if (hasCoordinates || hasCurrentLocation) {
                    val initialLat = lat.toDoubleOrNull() ?: currentLat ?: 0.0
                    val initialLon = lon.toDoubleOrNull() ?: currentLon ?: 0.0
                    val pickedLat = lat.toDoubleOrNull()
                    val pickedLon = lon.toDoubleOrNull()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clipToBounds(),
                        contentAlignment = Alignment.Center
                    ) {
                        MapPickerView(
                            initial = Pair(initialLat, initialLon),
                            currentLocation = if (currentLat != null && currentLon != null) {
                                Pair(currentLat, currentLon)
                            } else {
                                null
                            },
                            selected = if (pickedLat != null && pickedLon != null) {
                                Pair(pickedLat, pickedLon)
                            } else {
                                null
                            },
                            onPicked = { pLat, pLon ->
                                lat = pLat.toString()
                                lon = pLon.toString()
                            },
                            onReady = { isMapReady = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("mapPicker")
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.select_location_on_map_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_required)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text(stringResource(R.string.latitude_required)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lon,
                    onValueChange = { lon = it },
                    label = { Text(stringResource(R.string.longitude_required)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.category),
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = category == "amenity",
                        onClick = {
                            category = "amenity"
                            categoryValue = ""
                        },
                        label = { Text(stringResource(R.string.amenity)) }
                    )
                    FilterChip(
                        selected = category == "shop",
                        onClick = {
                            category = "shop"
                            categoryValue = ""
                        },
                        label = { Text(stringResource(R.string.shop)) }
                    )
                    FilterChip(
                        selected = category == "tourism",
                        onClick = {
                            category = "tourism"
                            categoryValue = ""
                        },
                        label = { Text(stringResource(R.string.tourism)) }
                    )
                }

                CategoryValueField(
                    category = category,
                    value = categoryValue,
                    onValueChange = { categoryValue = it }
                )

                OutlinedTextField(
                    value = additionalTags,
                    onValueChange = { additionalTags = it },
                    label = { Text(stringResource(R.string.additional_tags_label)) },
                    placeholder = { Text(stringResource(R.string.additional_tags_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text(
                    text = stringResource(R.string.osm_accuracy_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).testTag("cancelButton")
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            val latValue = lat.toDoubleOrNull()
                            val lonValue = lon.toDoubleOrNull()
                            val isInputValid = latValue != null && lonValue != null &&
                                name.isNotBlank() && categoryValue.isNotBlank()
                            if (isInputValid) {
                                val tags = mutableMapOf(
                                    "name" to name,
                                    category to categoryValue
                                )

                                // 追加タグをパース
                                if (additionalTags.isNotBlank()) {
                                    additionalTags.split(",").forEach { tagPair ->
                                        if (tagPair.contains("=")) {
                                            val parts = tagPair.trim().split("=")
                                            if (parts.size == 2) {
                                                tags[parts[0].trim()] = parts[1].trim()
                                            }
                                        }
                                    }
                                }

                                isSaving = true
                                onSave(latValue, lonValue, tags)
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("saveButton"),
                        enabled = !isSaving &&
                            lat.toDoubleOrNull() != null &&
                            lon.toDoubleOrNull() != null &&
                            name.isNotBlank() &&
                            categoryValue.isNotBlank()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.save_to_osm))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // マップ準備完了まで画面中央にローディングを表示
            if (!isMapReady) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryValueField(
    category: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = when (category) {
        "amenity" -> listOf(
            stringResource(R.string.amenity_restaurant),
            stringResource(R.string.amenity_cafe),
            stringResource(R.string.amenity_fast_food),
            stringResource(R.string.amenity_bar),
            stringResource(R.string.amenity_pub),
            stringResource(R.string.amenity_convenience),
            stringResource(R.string.amenity_vending_machine),
            stringResource(R.string.amenity_parking),
            stringResource(R.string.amenity_bench),
            stringResource(R.string.amenity_toilets),
            stringResource(R.string.amenity_post_box)
        )
        "shop" -> listOf(
            stringResource(R.string.shop_convenience),
            stringResource(R.string.shop_supermarket),
            stringResource(R.string.shop_clothes),
            stringResource(R.string.shop_hairdresser),
            stringResource(R.string.shop_bakery),
            stringResource(R.string.shop_drugstore)
        )
        "tourism" -> listOf(
            stringResource(R.string.tourism_information),
            stringResource(R.string.tourism_attraction),
            stringResource(R.string.tourism_viewpoint),
            stringResource(R.string.tourism_hotel),
            stringResource(R.string.tourism_museum),
            stringResource(R.string.tourism_artwork)
        )
        else -> emptyList()
    }

    val filteredOptions by remember(value, options) {
        derivedStateOf {
            if (value.isEmpty()) {
                options
            } else {
                options.filter { it.contains(value, ignoreCase = true) }
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.category_value_label, category)) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                .testTag("categoryValueField"),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onValueChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("TooGenericExceptionCaught")
private fun MapPickerView(
    initial: Pair<Double, Double>,
    currentLocation: Pair<Double, Double>?,
    selected: Pair<Double, Double>?,
    onPicked: (Double, Double) -> Unit,
    onReady: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetZoom = TARGET_ZOOM

    AndroidView(
        factory = { ctx ->
            try {
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(targetZoom)
                    controller.setCenter(GeoPoint(initial.first, initial.second))

                    // 親のスクロールを抑制するための設定
                    setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                v.parent.requestDisallowInterceptTouchEvent(true)
                            }
                            MotionEvent.ACTION_UP -> {
                                v.parent.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                        false
                    }

                    // 現在地のマーカー
                    currentLocation?.let {
                        val marker = Marker(this)
                        marker.position = GeoPoint(it.first, it.second)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        marker.icon = context.getDrawable(R.drawable.ic_current_location)
                        marker.title = context.getString(R.string.current_location)
                        overlays.add(marker)
                    }

                    // 既存選択位置のマーカー
                    selected?.let {
                        val marker = Marker(this)
                        marker.position = GeoPoint(it.first, it.second)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.icon = context.getDrawable(R.drawable.ic_selected_place)
                        marker.title = context.getString(R.string.selected_place)
                        overlays.add(marker)
                    }

                    // タップイベントで位置を拾う
                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p ?: return false
                            onPicked(p.latitude, p.longitude)
                            // マーカーの更新
                            overlays.removeAll {
                                it is Marker && (it.title == context.getString(R.string.selected_place) ||
                                    it.title == context.getString(R.string.current_location))
                            }

                            // 現在地マーカーを再描画
                            currentLocation?.let {
                                val curMarker = Marker(this@apply)
                                curMarker.position = GeoPoint(it.first, it.second)
                                curMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                curMarker.icon = context.getDrawable(R.drawable.ic_current_location)
                                curMarker.title = context.getString(R.string.current_location)
                                overlays.add(curMarker)
                            }

                            val newMarker = Marker(this@apply)
                            newMarker.position = GeoPoint(p.latitude, p.longitude)
                            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            newMarker.icon = context.getDrawable(R.drawable.ic_selected_place)
                            newMarker.title = context.getString(R.string.selected_place)
                            overlays.add(newMarker)
                            invalidate()
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?) = false
                    })
                    overlays.add(eventsOverlay)

                    // 地図の準備完了を通知
                    addOnFirstLayoutListener { _, _, _, _, _ ->
                        // タイルのロードを少し待つために遅延を入れる
                        postDelayed({
                            onReady()
                        }, MAP_READY_DELAY_MS)
                    }

                    // 確実に描画されるようにタイルのプリロードなどを設定（OSMの推奨設定に近いもの）
                    setHasTransientState(true)
                }
            } catch (e: Exception) {
                Timber.w(e, "MapView initialization failed")
                onReady() // 失敗時もローディングを消す
                FrameLayout(ctx)
            }
        },
        update = { view ->
            val mapView = view as? MapView ?: return@AndroidView
            try {
                // Remove hardcoded zoom/center reset to prevent resetting on every tap/recomposition
                // mapView.controller.setZoom(targetZoom)
                // mapView.controller.setCenter(GeoPoint(initial.first, initial.second))

                mapView.overlays.removeAll {
                    it is Marker && it.title == mapView.context.getString(R.string.selected_place)
                }
                selected?.let {
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(it.first, it.second)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.icon = mapView.context.getDrawable(R.drawable.ic_selected_place)
                    marker.title = mapView.context.getString(R.string.selected_place)
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            } catch (e: Exception) {
                Timber.d(e, "MapView update skipped")
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
