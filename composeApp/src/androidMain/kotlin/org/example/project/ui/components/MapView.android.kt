package org.example.project.ui.components

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.CameraUpdateFactory
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

private fun PlaceType.pinColor(): Int = when (this) {
    PlaceType.MUSEUM     -> android.graphics.Color.parseColor("#9C6ED6") // purple
    PlaceType.CAFE       -> android.graphics.Color.parseColor("#C8793A") // brown-orange
    PlaceType.CLUB       -> android.graphics.Color.parseColor("#D44F7A") // pink
    PlaceType.RESTAURANT -> android.graphics.Color.parseColor("#E8534A") // red-orange
}

private fun PlaceType.pinEmoji(): String = when (this) {
    PlaceType.MUSEUM     -> "🏛"
    PlaceType.CAFE       -> "☕"
    PlaceType.CLUB       -> "🎵"
    PlaceType.RESTAURANT -> "🍴"
}

/** Draws a teardrop pin bitmap. Call BitmapDescriptorFactory.fromBitmap() only inside GoogleMap content. */
private fun createPlaceBitmap(type: PlaceType): Bitmap {
    val w = 96
    val h = 128
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = type.pinColor() }
    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }

    val radius = w / 2f
    canvas.drawCircle(w / 2f, radius, radius, bgPaint)
    val tip = Path().apply {
        moveTo(w * 0.25f, h * 0.55f)
        lineTo(w * 0.75f, h * 0.55f)
        lineTo(w * 0.5f, h.toFloat())
        close()
    }
    canvas.drawPath(tip, bgPaint)
    canvas.drawCircle(w / 2f, radius, radius * 0.52f, whitePaint)

    val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(type.pinEmoji(), w / 2f, radius + 10f, emojiPaint)

    return bitmap
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

    // Build raw bitmaps outside GoogleMap (no Maps SDK dependency)
    val placeBitmaps = remember {
        PlaceType.entries.associateWith { createPlaceBitmap(it) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_POSITION, DEFAULT_ZOOM)
    }

    // Când evenimentele se încarcă, mută camera la primul eveniment
    LaunchedEffect(events) {
        val first = events.firstOrNull() ?: return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(LatLng(first.lat, first.lng), DEFAULT_ZOOM)
        )
    }

    // Load places on first render
    LaunchedEffect(Unit) {
        val target = cameraPositionState.position.target
        placesViewModel.loadPlaces(target.latitude, target.longitude)
    }

    // Reload when camera stops moving
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

        // Place markers — custom teardrop pin per type
        // BitmapDescriptorFactory is called HERE (inside GoogleMap) where Maps SDK is initialized
        places.forEach { place ->
            val icon = placeBitmaps[place.type]?.let { BitmapDescriptorFactory.fromBitmap(it) }
            Marker(
                state = MarkerState(LatLng(place.lat, place.lng)),
                title = place.name,
                snippet = place.address,
                icon = icon,
                onClick = { onPlaceClick(place); false }
            )
        }
    }
}
