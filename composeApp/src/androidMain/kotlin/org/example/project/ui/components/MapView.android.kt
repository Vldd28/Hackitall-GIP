package org.example.project.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.example.project.data.model.Event

private val DEFAULT_POSITION = LatLng(52.3676, 4.9041)
private const val DEFAULT_ZOOM = 12f

// Stil custom bazat pe culorile #31363F, #76ABAE, #EEEEEE
private val WANDR_MAP_STYLE = MapStyleOptions("""
[
  { "elementType": "geometry",
    "stylers": [{ "color": "#31363F" }] },
  { "elementType": "labels.text.stroke",
    "stylers": [{ "color": "#31363F" }] },
  { "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "water",
    "elementType": "geometry",
    "stylers": [{ "color": "#76ABAE" }] },
  { "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "road",
    "elementType": "geometry",
    "stylers": [{ "color": "#3E4550" }] },
  { "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#212830" }] },
  { "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [{ "color": "#76ABAE" }] },
  { "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#1F2835" }] },
  { "featureType": "road.highway",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "poi",
    "elementType": "geometry",
    "stylers": [{ "color": "#3A3F47" }] },
  { "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [{ "color": "#3D5465" }] },
  { "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#76ABAE" }] },
  { "featureType": "transit",
    "elementType": "geometry",
    "stylers": [{ "color": "#2F3540" }] },
  { "featureType": "transit.station",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "administrative",
    "elementType": "geometry",
    "stylers": [{ "color": "#76ABAE" }] },
  { "featureType": "administrative.country",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] }
]
""".trimIndent())

@Composable
actual fun MapView(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier
) {
    val startPosition = remember(events) {
        events.firstOrNull()
            ?.let { LatLng(it.lat, it.lng) }
            ?: DEFAULT_POSITION
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPosition, DEFAULT_ZOOM)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = WANDR_MAP_STYLE),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false
        )
    ) {
        events.forEach { event ->
            Marker(
                state = MarkerState(position = LatLng(event.lat, event.lng)),
                title = event.title,
                snippet = event.locationName,
                onClick = {
                    onEventClick(event)
                    false
                }
            )
        }
    }
}
