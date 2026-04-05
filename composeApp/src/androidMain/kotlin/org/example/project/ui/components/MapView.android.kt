package org.example.project.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
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
import org.example.project.viewmodel.PlacesViewModel

private val DEFAULT_POSITION = LatLng(44.4268, 26.1025) // Bucharest
private const val DEFAULT_ZOOM = 14f
private const val MIN_ZOOM_FOR_PLACES = 14f

private val WANDR_MAP_STYLE = MapStyleOptions("""
[
  { "elementType": "geometry", "stylers": [{ "color": "#31363F" }] },
  { "elementType": "labels.text.stroke", "stylers": [{ "color": "#31363F" }] },
  { "elementType": "labels.text.fill", "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
  { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#76ABAE" }] },
  { "featureType": "water", "elementType": "labels.text.fill", "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#3E4550" }] },
  { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#212830" }] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [{ "color": "#76ABAE" }] },
  { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [{ "color": "#EEEEEE" }] },
  { "featureType": "transit", "stylers": [{ "visibility": "off" }] },
  { "featureType": "administrative", "elementType": "geometry", "stylers": [{ "color": "#76ABAE" }] },
  { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{ "color": "#EEEEEE" }] }
]
""".trimIndent())

private fun PlaceType.pinColor(): Int = when (this) {
    PlaceType.MUSEUM     -> android.graphics.Color.parseColor("#9C6ED6")
    PlaceType.CAFE       -> android.graphics.Color.parseColor("#C8793A")
    PlaceType.CLUB       -> android.graphics.Color.parseColor("#D44F7A")
    PlaceType.RESTAURANT -> android.graphics.Color.parseColor("#E8534A")
}

private fun createPlaceBitmap(type: PlaceType): Bitmap {
    val w = 48; val h = 64
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = type.pinColor() }
    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
    val radius = w / 2f
    canvas.drawCircle(w / 2f, radius, radius, bgPaint)
    val tip = Path().apply {
        moveTo(w * 0.25f, h * 0.55f); lineTo(w * 0.75f, h * 0.55f)
        lineTo(w * 0.5f, h.toFloat()); close()
    }
    canvas.drawPath(tip, bgPaint)
    canvas.drawCircle(w / 2f, radius, radius * 0.52f, whitePaint)
    val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14f; textAlign = Paint.Align.CENTER
    }
    canvas.drawText(type.emoji, w / 2f, radius + 5f, emojiPaint)
    return bitmap
}

@Composable
actual fun MapView(
    events: List<Event>,
    userId: String,
    onEventClick: (Event) -> Unit,
    onPlaceClick: (PlaceResult) -> Unit,
    searchQuery: String,
    onSearchConsumed: () -> Unit,
    modifier: Modifier
) {
    val placesViewModel = viewModel<PlacesViewModel>()
    val places by placesViewModel.places.collectAsState()
    val searchResult by placesViewModel.searchResult.collectAsState()
    var selectedPlace by remember { mutableStateOf<PlaceResult?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    val placeBitmaps = remember { PlaceType.entries.associateWith { createPlaceBitmap(it) } }
    val placeIcons = remember { mutableMapOf<PlaceType, BitmapDescriptor>() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_POSITION, DEFAULT_ZOOM)
    }

    // Trigger text search when searchQuery changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            placesViewModel.searchByText(searchQuery)
            onSearchConsumed()
        }
    }

    // Navigate to search result and show its bottom sheet
    LaunchedEffect(searchResult) {
        val result = searchResult ?: return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(LatLng(result.lat, result.lng), 16f)
        )
        selectedPlace = result
        placesViewModel.clearSearchResult()
    }

    LaunchedEffect(events) {
        val first = events.firstOrNull() ?: return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(LatLng(first.lat, first.lng), DEFAULT_ZOOM)
        )
    }

    val currentZoom = cameraPositionState.position.zoom
    val showPlaces = currentZoom >= MIN_ZOOM_FOR_PLACES

    fun radiusForZoom(zoom: Float): Double = when {
        zoom >= 17f -> 300.0; zoom >= 16f -> 600.0; zoom >= 15f -> 1000.0; else -> 1500.0
    }

    LaunchedEffect(Unit) {
        if (showPlaces) {
            val target = cameraPositionState.position.target
            placesViewModel.loadPlaces(target.latitude, target.longitude, radiusForZoom(currentZoom))
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && showPlaces) {
            val target = cameraPositionState.position.target
            placesViewModel.loadPlaces(target.latitude, target.longitude, radiusForZoom(cameraPositionState.position.zoom))
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = WANDR_MAP_STYLE),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
    ) {
        events.forEach { event ->
            Marker(
                state = MarkerState(LatLng(event.lat, event.lng)),
                title = event.title,
                snippet = event.locationName,
                onClick = { selectedEvent = event; false }
            )
        }

        if (showPlaces) {
            places.forEach { place ->
                val icon = placeIcons.getOrPut(place.type) {
                    BitmapDescriptorFactory.fromBitmap(placeBitmaps[place.type]!!)
                }
                Marker(
                    state = MarkerState(LatLng(place.lat, place.lng)),
                    title = place.name,
                    snippet = place.address,
                    icon = icon,
                    onClick = { selectedPlace = place; false }
                )
            }
        }
    }

    selectedPlace?.let { place ->
        PlaceBottomSheet(
            place = place, allEvents = events, userId = userId,
            onDismiss = { selectedPlace = null }
        )
    }

    selectedEvent?.let { event ->
        EventBottomSheet(
            tappedEvent = event, allEvents = events, userId = userId,
            onDismiss = { selectedEvent = null }
        )
    }
}
