package org.example.project.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.example.project.data.model.Event
import org.example.project.data.model.PlaceResult

private const val DEFAULT_LAT = 52.3676
private const val DEFAULT_LNG = 4.9041
private const val DEFAULT_ZOOM = 12

@Composable
actual fun MapView(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onPlaceClick: (PlaceResult) -> Unit,
    modifier: Modifier
) {
    val firstEvent = events.firstOrNull()
    val lat = firstEvent?.lat ?: DEFAULT_LAT
    val lng = firstEvent?.lng ?: DEFAULT_LNG

    // Afișează harta Leaflet deasupra canvas-ului Compose
    DisposableEffect(Unit) {
        jsShowMap(lat, lng, DEFAULT_ZOOM)
        onDispose {
            jsHideMap()
        }
    }

    // Actualizează markere când se schimbă evenimentele
    LaunchedEffect(events) {
        jsClearMarkers()
        events.forEach { event ->
            jsAddMarker(event.lat, event.lng, event.title, event.locationName)
        }
    }

    // Box gol — harta e în div-ul HTML deasupra canvas-ului
    Box(modifier = modifier.fillMaxSize())
}
