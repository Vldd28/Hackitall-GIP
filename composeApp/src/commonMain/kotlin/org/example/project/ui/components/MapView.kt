package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.data.model.Event
import org.example.project.data.model.PlaceResult

@Composable
expect fun MapView(
    events: List<Event>,
    userId: String,
    onEventClick: (Event) -> Unit,
    onPlaceClick: (PlaceResult) -> Unit,
    searchQuery: String,
    onSearchConsumed: () -> Unit,
    modifier: Modifier = Modifier
)
