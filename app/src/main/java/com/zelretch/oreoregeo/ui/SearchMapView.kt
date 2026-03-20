package com.zelretch.oreoregeo.ui

import android.graphics.Color
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.zelretch.oreoregeo.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

private const val ZOOM_VERY_CLOSE = 18.5
private const val ZOOM_CLOSE = 18.0
private const val ZOOM_MEDIUM = 17.0
private const val ZOOM_FAR = 16.5
private const val ZOOM_VERY_FAR = 16.0
private const val ZOOM_DEFAULT = 15.5

private const val RADIUS_VERY_CLOSE = 50
private const val RADIUS_CLOSE = 100
private const val RADIUS_MEDIUM = 200
private const val RADIUS_FAR = 300
private const val RADIUS_VERY_FAR = 400

private const val CIRCLE_ALPHA = 50
private const val CIRCLE_COLOR_R = 0
private const val CIRCLE_COLOR_G = 0
private const val CIRCLE_COLOR_B = 255

@Composable
@Suppress("FunctionNaming")
fun SearchMapView(
    location: Pair<Double, Double>,
    radiusMeters: Int,
    selectedPlaceLocation: Pair<Double, Double>? = null
) {
    val targetZoom = calculateZoomLevel(radiusMeters)

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(targetZoom)
                controller.setCenter(GeoPoint(location.first, location.second))

                setupScrollInterception()
                addCurrentLocationMarker(location, context.getString(R.string.current_location))
                addSearchRadiusCircle(location, radiusMeters)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            if (selectedPlaceLocation == null) {
                mapView.controller.setZoom(targetZoom)
            }

            mapView.addCurrentLocationMarker(location, mapView.context.getString(R.string.current_location))
            mapView.addSearchRadiusCircle(location, radiusMeters)

            selectedPlaceLocation?.let {
                mapView.addSelectedPlaceMarker(it, mapView.context.getString(R.string.selected_place))
                mapView.controller.animateTo(GeoPoint(it.first, it.second))
            } ?: run {
                mapView.controller.setCenter(GeoPoint(location.first, location.second))
            }
            mapView.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun calculateZoomLevel(radiusMeters: Int): Double = when {
    radiusMeters <= RADIUS_VERY_CLOSE -> ZOOM_VERY_CLOSE
    radiusMeters <= RADIUS_CLOSE -> ZOOM_CLOSE
    radiusMeters <= RADIUS_MEDIUM -> ZOOM_MEDIUM
    radiusMeters <= RADIUS_FAR -> ZOOM_FAR
    radiusMeters <= RADIUS_VERY_FAR -> ZOOM_VERY_FAR
    else -> ZOOM_DEFAULT
}

private fun MapView.setupScrollInterception() {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
}

private fun MapView.addCurrentLocationMarker(location: Pair<Double, Double>, title: String) {
    val marker = Marker(this)
    marker.position = GeoPoint(location.first, location.second)
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    marker.icon = context.getDrawable(R.drawable.ic_current_location)
    marker.title = title
    overlays.add(marker)
}

private fun MapView.addSearchRadiusCircle(location: Pair<Double, Double>, radiusMeters: Int) {
    val circle = Polygon.pointsAsCircle(GeoPoint(location.first, location.second), radiusMeters.toDouble())
    val circleOverlay = Polygon(this)
    circleOverlay.points = circle
    circleOverlay.fillPaint.color = Color.argb(
        CIRCLE_ALPHA,
        CIRCLE_COLOR_R,
        CIRCLE_COLOR_G,
        CIRCLE_COLOR_B
    )
    circleOverlay.outlinePaint.color = Color.BLUE
    circleOverlay.outlinePaint.strokeWidth = 2f
    circleOverlay.infoWindow = null
    overlays.add(circleOverlay)
}

private fun MapView.addSelectedPlaceMarker(location: Pair<Double, Double>, title: String) {
    val selectedMarker = Marker(this)
    selectedMarker.position = GeoPoint(location.first, location.second)
    selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    selectedMarker.icon = context.getDrawable(R.drawable.ic_selected_place)
    selectedMarker.title = title
    overlays.add(selectedMarker)
}
