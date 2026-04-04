package org.example.project.ui.components

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import org.example.project.data.model.PlaceResult
import org.example.project.data.model.PlaceType
import org.example.project.data.repository.PlacesRepository
import org.example.project.viewmodel.PlacesViewModel

private val DEFAULT_POSITION = LatLng(44.4268, 26.1025) // Bucharest
private const val DEFAULT_ZOOM = 14f

private val WANDR_MAP_STYLE = MapStyleOptions("""
[
  { "elementType": "geometry",
    "stylers": [{ "color": "#31363F" }] },
  { "elementType": "labels.text.stroke",
    "stylers": [{ "color": "#31363F" }] },
  { "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "poi",
    "stylers": [{ "visibility": "off" }] },
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
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "transit",
    "stylers": [{ "visibility": "off" }] },
  { "featureType": "administrative",
    "elementType": "geometry",
    "stylers": [{ "color": "#76ABAE" }] },
  { "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#EEEEEE" }] }
]
""".trimIndent())

private fun PlaceType.markerHue(): Float = when (this) {
    PlaceType.MUSEUM     -> BitmapDescriptorFactory.HUE_VIOLET
    PlaceType.CAFE       -> BitmapDescriptorFactory.HUE_ORANGE
    PlaceType.CLUB       -> BitmapDescriptorFactory.HUE_ROSE
    PlaceType.RESTAURANT -> BitmapDescriptorFactory.HUE_YELLOW
}

@Composable
actual fun MapView(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onPlaceClick: (PlaceResult) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val apiKey = remember {
        try {
            context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                .metaData
                ?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) { "" }
    }

    val placesViewModel = viewModel<PlacesViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PlacesViewModel(PlacesRepository(apiKey)) as T
        }
    )

    val places by placesViewModel.places.collectAsState()

    val startPosition = remember(events) {
        events.firstOrNull()?.let { LatLng(it.lat, it.lng) } ?: DEFAULT_POSITION
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPosition, DEFAULT_ZOOM)
    }

    // Load places on initial load and whenever camera stops moving
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            placesViewModel.loadPlaces(target.latitude, target.longitude)
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = WANDR_MAP_STYLE),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
    ) {
        // Wandr event markers — default red pin
        events.forEach { event ->
            Marker(
                state = MarkerState(LatLng(event.lat, event.lng)),
                title = event.title,
                snippet = event.locationName,
                onClick = { onEventClick(event); false }
            )
        }

        // Place markers — colored by type
        places.forEach { place ->
            Marker(
                state = MarkerState(LatLng(place.lat, place.lng)),
                title = place.name,
                snippet = place.address,
                icon = BitmapDescriptorFactory.defaultMarker(place.type.markerHue()),
                onClick = { onPlaceClick(place); false }
            )
        }
    }
}
