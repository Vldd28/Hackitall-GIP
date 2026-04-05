package org.example.project.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.example.project.data.model.Event
import org.example.project.data.model.PlaceResult
import org.example.project.data.model.PlaceType
import org.example.project.viewmodel.PlacesViewModel
import kotlin.math.*

private const val DEFAULT_LAT  = 44.4268
private const val DEFAULT_LNG  = 26.1025
private const val DEFAULT_ZOOM = 14
private const val MIN_ZOOM_FOR_PLACES = 14.0

private fun PlaceType.pinColor(): String = when (this) {
    PlaceType.MUSEUM     -> "#9C6ED6"
    PlaceType.CAFE       -> "#C8793A"
    PlaceType.CLUB       -> "#D44F7A"
    PlaceType.RESTAURANT -> "#E8534A"
}

private fun radiusForZoom(zoom: Double): Double = when {
    zoom >= 17.0 -> 300.0
    zoom >= 16.0 -> 600.0
    zoom >= 15.0 -> 1000.0
    else         -> 1500.0
}

private fun webDistMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6371000.0
    val dLat = PI / 180.0 * (lat2 - lat1)
    val dLon = PI / 180.0 * (lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(PI / 180.0 * lat1) * cos(PI / 180.0 * lat2) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
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
    modifier: Modifier
) {
    val firstEvent = events.firstOrNull()
    val startLat = firstEvent?.lat ?: DEFAULT_LAT
    val startLng = firstEvent?.lng ?: DEFAULT_LNG

    val placesViewModel = viewModel { PlacesViewModel() }
    val places by placesViewModel.places.collectAsState()

    var selectedPlace by remember { mutableStateOf<PlaceResult?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    var lastLoadLat  by remember { mutableStateOf(DEFAULT_LAT) }
    var lastLoadLng  by remember { mutableStateOf(DEFAULT_LNG) }
    var lastLoadZoom by remember { mutableStateOf(DEFAULT_ZOOM.toDouble()) }

    // ── Show / hide the Leaflet overlay ──────────────────────────────────────
    DisposableEffect(Unit) {
        jsShowMap(startLat, startLng, DEFAULT_ZOOM)
        onDispose { jsHideMap() }
    }

    // ── Raise/lower canvas when a bottom sheet is open ────────────────────────
    LaunchedEffect(selectedPlace, selectedEvent) {
        if (selectedPlace != null || selectedEvent != null) {
            jsShowBottomSheet()
        } else {
            jsRestoreMapLayout()
        }
    }

    // ── Center on target coordinates ───────────────────────────────────────
    LaunchedEffect(centerLat, centerLng) {
        if (centerLat != null && centerLng != null) {
            jsPanTo(centerLat, centerLng, 16)
            onCenterConsumed()
        }
    }

    // ── Event markers (only for events not shown via a loaded place) ─────────
    LaunchedEffect(events, places) {
        jsClearEventMarkers()
        val placeNames = places.map { it.name }.toSet()
        events.filter { it.locationName !in placeNames }.forEach { event ->
            jsAddEventMarker(event.lat, event.lng, event.title, event.locationName, event.id)
        }
    }

    // ── Place markers (with event count badges) ──────────────────────────────
    LaunchedEffect(places, events) {
        jsClearPlaceMarkers()
        places.forEach { place ->
            val eventCount = events.count { it.locationName == place.name }
            jsAddPlaceMarker(
                place.lat, place.lng,
                place.type.emoji, place.type.pinColor(),
                place.name, place.address, place.id,
                eventCount
            )
        }
    }

    // ── Initial places load (after map init) ──────────────────────────────────
    LaunchedEffect(Unit) {
        delay(800)
        val zoom = jsGetZoom()
        if (zoom >= MIN_ZOOM_FOR_PLACES) {
            val lat = jsGetCenterLat(); val lng = jsGetCenterLng()
            lastLoadLat = lat; lastLoadLng = lng; lastLoadZoom = zoom
            placesViewModel.loadPlaces(lat, lng, radiusForZoom(zoom))
        }
    }

    // ── Polling loop: reload on pan/zoom + detect marker clicks ───────────────
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)

            val zoom = jsGetZoom()
            val lat  = jsGetCenterLat()
            val lng  = jsGetCenterLng()

            val zoomChanged = abs(zoom - lastLoadZoom) >= 1.0
            val moved       = webDistMeters(lat, lng, lastLoadLat, lastLoadLng) > 300.0
            if ((zoomChanged || moved) && zoom >= MIN_ZOOM_FOR_PLACES) {
                lastLoadLat = lat; lastLoadLng = lng; lastLoadZoom = zoom
                placesViewModel.loadPlaces(lat, lng, radiusForZoom(zoom))
            }
            if (zoom < MIN_ZOOM_FOR_PLACES) jsClearPlaceMarkers()

            // Don't process clicks while a sheet is open
            if (selectedPlace != null || selectedEvent != null) continue

            val eventId = jsPollEventClickId()?.toString()
            if (eventId != null) selectedEvent = events.find { it.id == eventId }

            val placeId = jsPollPlaceClickId()?.toString()
            if (placeId != null) selectedPlace = places.find { it.id == placeId }
        }
    }

    Box(modifier = modifier.fillMaxSize())

    selectedPlace?.let { place ->
        PlaceBottomSheet(
            place = place, allEvents = events, userId = userId,
            onDismiss = { selectedPlace = null },
            onEventCreated = onEventCreated
        )
    }

    selectedEvent?.let { event ->
        EventBottomSheet(
            tappedEvent = event, allEvents = events, userId = userId,
            onDismiss = { selectedEvent = null }
        )
    }
}
