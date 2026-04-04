package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.data.model.Event

@Composable
expect fun MapView(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
)
