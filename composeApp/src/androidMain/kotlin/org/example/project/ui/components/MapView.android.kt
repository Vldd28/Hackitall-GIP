package org.example.project.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
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

// Special events with red pins (always the same 10)
private val RED_PIN_EVENT_IDS = setOf(
    "fe835a07-5b96-4d95-bcce-0bd0d1788455", // Street Art Walking Tour
    "9a0994bc-296c-44d2-9048-d8537ca2df1a", // Jazz Evening at Paradiso
    "ea7ea491-f8e3-4c73-bbf0-884b8d1e48b4", // Kotlin & KMP Meetup Amsterdam
    "fd0f7e1c-233e-4671-b520-c749bd3d9b9d", // Vondelpark Morning Run
    "red-event-5", // Placeholder IDs for 10 total
    "red-event-6",
    "red-event-7",
    "red-event-8",
    "red-event-9",
    "red-event-10"
)

private val WANDR_MAP_STYLE = MapStyleOptions("""
[
  { "elementType": "geometry", "stylers": [{ "color": "#EAF5EC" }] },
  { "elementType": "labels.text.stroke", "stylers": [{ "color": "#EAF5EC" }] },
  { "elementType": "labels.text.fill", "stylers": [{ "color": "#3A5A40" }] },
  { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
  { "featureType": "poi.park", "elementType": "geometry", "stylers": [{ "color": "#C8E6C9" }] },
  { "featureType": "poi.park", "stylers": [{ "visibility": "on" }] },
  { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#B2DFDB" }] },
  { "featureType": "water", "elementType": "labels.text.fill", "stylers": [{ "color": "#3A5A40" }] },
  { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#FFFFFF" }] },
  { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#C8E6C9" }] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [{ "color": "#B4DEBD" }] },
  { "featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [{ "color": "#91C4A0" }] },
  { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [{ "color": "#3A5A40" }] },
  { "featureType": "transit", "stylers": [{ "visibility": "off" }] },
  { "featureType": "administrative", "elementType": "geometry", "stylers": [{ "color": "#91C4A0" }] },
  { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{ "color": "#3A5A40" }] },
  { "featureType": "landscape", "elementType": "geometry", "stylers": [{ "color": "#EAF5EC" }] }
]
""".trimIndent())

private val WANDR_DARK_MAP_STYLE = MapStyleOptions("""
[
  { "elementType": "geometry", "stylers": [{ "color": "#070F2B" }] },
  { "elementType": "labels.text.stroke", "stylers": [{ "color": "#070F2B" }] },
  { "elementType": "labels.text.fill", "stylers": [{ "color": "#DCDCEB" }] },
  { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
  { "featureType": "poi.park", "elementType": "geometry", "stylers": [{ "color": "#1B1A55" }] },
  { "featureType": "poi.park", "stylers": [{ "visibility": "on" }] },
  { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#1B1A55" }] },
  { "featureType": "water", "elementType": "labels.text.fill", "stylers": [{ "color": "#DCDCEB" }] },
  { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#535C91" }] },
  { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#1B1A55" }] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [{ "color": "#535C91" }] },
  { "featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [{ "color": "#1B1A55" }] },
  { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [{ "color": "#DCDCEB" }] },
  { "featureType": "transit", "stylers": [{ "visibility": "off" }] },
  { "featureType": "administrative", "elementType": "geometry", "stylers": [{ "color": "#1B1A55" }] },
  { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{ "color": "#DCDCEB" }] },
  { "featureType": "landscape", "elementType": "geometry", "stylers": [{ "color": "#070F2B" }] }
]
""".trimIndent())

private fun PlaceType.pinColor(isDarkMode: Boolean): Int = when (this) {
    PlaceType.MUSEUM     -> if (isDarkMode) android.graphics.Color.parseColor("#535C91") else android.graphics.Color.parseColor("#7B9E87")
    PlaceType.CAFE       -> if (isDarkMode) android.graphics.Color.parseColor("#6A7AB5") else android.graphics.Color.parseColor("#91C4A0")
    PlaceType.CLUB       -> if (isDarkMode) android.graphics.Color.parseColor("#4A5A8A") else android.graphics.Color.parseColor("#6AAF8A")
    PlaceType.RESTAURANT -> if (isDarkMode) android.graphics.Color.parseColor("#3A4A7A") else android.graphics.Color.parseColor("#4E9A6F")
}

private fun createEventPinBitmap(count: Int, isDarkMode: Boolean, isRedPin: Boolean = false): Bitmap {
    val w = 72; val h = 92
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        color = when {
            isRedPin -> android.graphics.Color.parseColor("#E74C3C") // Red for special events
            isDarkMode -> android.graphics.Color.parseColor("#535C91")
            else -> android.graphics.Color.parseColor("#5A8A6A")
        }
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = when {
            isRedPin -> android.graphics.Color.parseColor("#C0392B") // Dark red stroke
            isDarkMode -> android.graphics.Color.parseColor("#1B1A55")
            else -> android.graphics.Color.parseColor("#2D4A35")
        }
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    val radius = w / 2f
    canvas.drawCircle(w / 2f, radius, radius - 2f, bgPaint)
    canvas.drawCircle(w / 2f, radius, radius - 2f, strokePaint)
    val tip = Path().apply {
        moveTo(w * 0.28f, h * 0.60f); lineTo(w * 0.72f, h * 0.60f)
        lineTo(w * 0.5f, h.toFloat() - 2f); close()
    }
    canvas.drawPath(tip, bgPaint)
    canvas.drawPath(tip, strokePaint)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = if (count > 1) 16f else 20f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val label = if (count > 1) "+$count" else "★"
    canvas.drawText(label, w / 2f, radius + 7f, textPaint)
    return bitmap
}

private fun createPlaceBitmap(type: PlaceType, isDarkMode: Boolean): Bitmap {
    val w = 48; val h = 64
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = type.pinColor(isDarkMode) }
    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        color = if (isDarkMode) android.graphics.Color.parseColor("#070F2B") else android.graphics.Color.WHITE
    }
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
    centerLat: Double?,
    centerLng: Double?,
    onCenterConsumed: () -> Unit,
    onEventCreated: (Event) -> Unit,
    onEventJoined: (Event) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier
) {
    val placesViewModel = viewModel<PlacesViewModel>()
    val places by placesViewModel.places.collectAsState()
    val searchResult by placesViewModel.searchResult.collectAsState()
    var selectedPlace by remember { mutableStateOf<PlaceResult?>(null) }

    val placeBitmaps = remember(isDarkMode) { PlaceType.entries.associateWith { createPlaceBitmap(it, isDarkMode) } }
    val placeIcons = remember(isDarkMode) { mutableMapOf<PlaceType, BitmapDescriptor>() }

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

    LaunchedEffect(centerLat, centerLng) {
        if (centerLat != null && centerLng != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(centerLat, centerLng), 16f)
            )
            onCenterConsumed()
        }
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

    // Group events by location (rounded to ~100m grid)
    val eventsByLocation = remember(events) {
        events.groupBy { Pair((it.lat * 1000).toInt(), (it.lng * 1000).toInt()) }
    }
    val eventPinBitmaps = remember(eventsByLocation, isDarkMode) {
        eventsByLocation.mapValues { (_, evts) -> 
            val hasRedEvent = evts.any { it.id in RED_PIN_EVENT_IDS }
            createEventPinBitmap(evts.size, isDarkMode, hasRedEvent)
        }
    }
    val eventPinIcons = remember(eventPinBitmaps) {
        eventPinBitmaps.mapValues { (_, bmp) -> BitmapDescriptorFactory.fromBitmap(bmp) }
    }

    Box(modifier = modifier.fillMaxSize()) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = if (isDarkMode) WANDR_DARK_MAP_STYLE else WANDR_MAP_STYLE),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, compassEnabled = false)
    ) {
        // Event location pins — clicking opens the place/location popup
        eventsByLocation.forEach { (key, evts) ->
            val rep = evts.first()
            val icon = eventPinIcons[key]
            Marker(
                state = MarkerState(LatLng(rep.lat, rep.lng)),
                title = rep.locationName,
                snippet = if (evts.size > 1) "${evts.size} events" else rep.title,
                icon = icon,
                onClick = {
                    // Find matching place or create a synthetic one to open PlaceBottomSheet
                    val matchingPlace = places.firstOrNull {
                        kotlin.math.abs(it.lat - rep.lat) < 0.001 && kotlin.math.abs(it.lng - rep.lng) < 0.001
                    } ?: PlaceResult(
                        id = rep.locationName,
                        name = rep.locationName,
                        lat = rep.lat,
                        lng = rep.lng,
                        type = PlaceType.RESTAURANT,
                        address = rep.locationName
                    )
                    selectedPlace = matchingPlace
                    false
                }
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
            onDismiss = { selectedPlace = null },
            onEventCreated = onEventCreated,
            onEventJoined = onEventJoined,
            isDarkMode = isDarkMode
        )
    }

    // Custom compass at top-center
    val bearing = cameraPositionState.position.bearing
    if (bearing != 0f) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .shadow(4.dp, CircleShape)
                .background(
                    if (isDarkMode) Color(27, 26, 85).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f),
                    CircleShape
                )
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Compass",
                tint = if (isDarkMode) Color(150, 220, 250) else Color(0xFF5A8A6A),
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = -bearing }
            )
        }
    }

    } // end outer Box
}
