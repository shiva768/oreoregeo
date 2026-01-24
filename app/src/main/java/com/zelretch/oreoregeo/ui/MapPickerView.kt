package com.zelretch.oreoregeo.ui

import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.zelretch.oreoregeo.R
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import timber.log.Timber

private const val TARGET_ZOOM = 17.0
private const val MAP_READY_DELAY_MS = 500L

@Composable
@Suppress("TooGenericExceptionCaught", "FunctionNaming", "LongMethod", "LongParameterList")
fun MapPickerView(
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

                    setupDisallowInterceptTouchEvent()
                    addLocationMarkers(currentLocation, selected)
                    addTapEventsOverlay(onPicked, currentLocation)

                    addOnFirstLayoutListener { _, _, _, _, _ ->
                        postDelayed({ onReady() }, MAP_READY_DELAY_MS)
                    }
                    setHasTransientState(true)
                }
            } catch (e: Exception) {
                Timber.w(e, "MapView initialization failed")
                onReady()
                FrameLayout(ctx)
            }
        },
        update = { view ->
            val mapView = view as? MapView ?: return@AndroidView
            try {
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

private fun MapView.setupDisallowInterceptTouchEvent() {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
}

private fun MapView.addLocationMarkers(
    currentLocation: Pair<Double, Double>?,
    selected: Pair<Double, Double>?
) {
    currentLocation?.let {
        val marker = Marker(this)
        marker.position = GeoPoint(it.first, it.second)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        marker.icon = context.getDrawable(R.drawable.ic_current_location)
        marker.title = context.getString(R.string.current_location)
        overlays.add(marker)
    }

    selected?.let {
        val marker = Marker(this)
        marker.position = GeoPoint(it.first, it.second)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = context.getDrawable(R.drawable.ic_selected_place)
        marker.title = context.getString(R.string.selected_place)
        overlays.add(marker)
    }
}

private fun MapView.addTapEventsOverlay(
    onPicked: (Double, Double) -> Unit,
    currentLocation: Pair<Double, Double>?
) {
    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            p ?: return false
            onPicked(p.latitude, p.longitude)
            overlays.removeAll {
                it is Marker && (
                    it.title == context.getString(R.string.selected_place) ||
                        it.title == context.getString(R.string.current_location)
                    )
            }

            addLocationMarkers(currentLocation, p.latitude to p.longitude)
            invalidate()
            return true
        }

        override fun longPressHelper(p: GeoPoint?) = false
    })
    overlays.add(eventsOverlay)
}
