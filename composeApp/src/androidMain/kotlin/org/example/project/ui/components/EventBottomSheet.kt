package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.data.model.Event
import org.example.project.data.model.EventParticipant
import org.example.project.data.repository.EventRepository

private val DarkBg    = Color(0xFF31363F)
private val TealColor = Color(0xFF76ABAE)
private val LightText = Color(0xFFEEEEEE)
private val SubText   = Color(0xFFAAAAAA)
private val CardBg    = Color(0xFF3A3F47)
private val RedColor  = Color(0xFFE8534A)

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun distMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
    val r = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, r)
    return r[0]
}

/** ISO-8601 string comparison works correctly for sorting (YYYY-MM-DDTHH:MM:SS). */
private fun nowIso(): String {
    val cal = java.util.Calendar.getInstance()
    return "%04d-%02d-%02dT%02d:%02d:%02d".format(
        cal.get(java.util.Calendar.YEAR),
        cal.get(java.util.Calendar.MONTH) + 1,
        cal.get(java.util.Calendar.DAY_OF_MONTH),
        cal.get(java.util.Calendar.HOUR_OF_DAY),
        cal.get(java.util.Calendar.MINUTE),
        cal.get(java.util.Calendar.SECOND)
    )
}

private fun formatDateTime(iso: String): String = try {
    // "2025-06-01T20:00:00" → "01 Jun 2025 · 20:00"
    val parts = iso.split("T")
    val dateParts = parts[0].split("-")
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val month = months.getOrElse(dateParts[1].toInt() - 1) { dateParts[1] }
    val time = parts.getOrElse(1) { "" }.take(5)
    "${dateParts[2]} $month ${dateParts[0]} · $time"
} catch (e: Exception) { iso }

// ── Main sheet ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBottomSheet(
    tappedEvent: Event,
    allEvents: List<Event>,
    userId: String,
    onDismiss: () -> Unit
) {
    val now = remember { nowIso() }

    // All events at this location (within 100m)
    val locationEvents = remember(tappedEvent, allEvents) {
        allEvents.filter { distMeters(it.lat, it.lng, tappedEvent.lat, tappedEvent.lng) <= 100f }
    }

    val upcomingEvents = remember(locationEvents) {
        locationEvents.filter { it.dateTime >= now }.sortedBy { it.dateTime }
    }
    val pastEvents = remember(locationEvents) {
        locationEvents.filter { it.dateTime < now }.sortedByDescending { it.dateTime }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkBg,
        contentColor = LightText,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SubText)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Location header
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        tappedEvent.locationName,
                        color = TealColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Upcoming events ───────────────────────────────────────────────
            if (upcomingEvents.isNotEmpty()) {
                item {
                    SectionHeader("Upcoming Events")
                }
                items(upcomingEvents) { event ->
                    EventCard(event = event, userId = userId, isUpcoming = true)
                }
            }

            // ── Past events ───────────────────────────────────────────────────
            if (pastEvents.isNotEmpty()) {
                item {
                    SectionHeader("Past Events")
                }
                items(pastEvents) { event ->
                    EventCard(event = event, userId = userId, isUpcoming = false)
                }
            }

            if (locationEvents.isEmpty()) {
                item {
                    Text(
                        "No events at this location yet.",
                        color = SubText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = LightText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

// ── Event card ────────────────────────────────────────────────────────────────

@Composable
private fun EventCard(event: Event, userId: String, isUpcoming: Boolean) {
    val repo = remember { EventRepository() }
    val scope = rememberCoroutineScope()

    var participants by remember { mutableStateOf<List<EventParticipant>>(emptyList()) }
    var hasJoined by remember { mutableStateOf(false) }
    var showPartyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(event.id) {
        runCatching {
            participants = repo.getEventParticipants(event.id)
            hasJoined = participants.any { it.profileId == userId }
        }
    }

    val spotsLeft = event.maxParticipants - participants.size
    val isFull = spotsLeft <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Placeholder image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF4A5060)),
                contentAlignment = Alignment.Center
            ) {
                Text("📍", fontSize = 24.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    color = LightText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    formatDateTime(event.dateTime),
                    color = TealColor,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))

                // Lobby bar
                LobbyRow(current = participants.size, max = event.maxParticipants)

                // Join buttons — only for upcoming events
                if (isUpcoming) {
                    Spacer(Modifier.height(8.dp))
                    if (hasJoined) {
                        Text("✓ Joined", color = TealColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    } else if (isFull) {
                        Text("Full", color = RedColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Join as person
                            Button(
                                onClick = {
                                    scope.launch {
                                        runCatching {
                                            repo.joinEvent(event.id, userId)
                                            participants = repo.getEventParticipants(event.id)
                                            hasJoined = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Join", fontSize = 12.sp)
                            }

                            // Join as party
                            OutlinedButton(
                                onClick = { showPartyDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TealColor),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TealColor),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Groups, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Party", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPartyDialog) {
        PartyJoinDialog(
            event = event,
            spotsLeft = spotsLeft,
            userId = userId,
            onConfirm = { partySize ->
                showPartyDialog = false
                scope.launch {
                    runCatching {
                        // Join once for the current user; party size noted in UI
                        repo.joinEvent(event.id, userId)
                        participants = repo.getEventParticipants(event.id)
                        hasJoined = true
                    }
                }
            },
            onDismiss = { showPartyDialog = false }
        )
    }
}

@Composable
private fun LobbyRow(current: Int, max: Int) {
    val fraction = if (max > 0) current.toFloat() / max else 0f
    val barColor = when {
        fraction >= 1f   -> RedColor
        fraction >= 0.75f -> Color(0xFFFFAA44)
        else             -> TealColor
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, null, tint = SubText, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text("$current / $max spots", color = SubText, fontSize = 12.sp)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = barColor,
            trackColor = Color(0xFF4A5060)
        )
    }
}

@Composable
private fun PartyJoinDialog(
    event: Event,
    spotsLeft: Int,
    userId: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var partySize by remember { mutableStateOf(2) }
    val maxParty = minOf(spotsLeft, 10)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBg,
        titleContentColor = LightText,
        textContentColor = LightText,
        title = { Text("Join as Party", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("How many people in your group?", color = SubText, fontSize = 13.sp)
                Text(
                    "$partySize people",
                    color = TealColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Slider(
                    value = partySize.toFloat(),
                    onValueChange = { partySize = it.toInt() },
                    valueRange = 2f..maxParty.toFloat().coerceAtLeast(2f),
                    steps = maxOf(0, maxParty - 3),
                    colors = SliderDefaults.colors(
                        thumbColor = TealColor,
                        activeTrackColor = TealColor,
                        inactiveTrackColor = CardBg
                    )
                )
                Text(
                    "$spotsLeft spots available",
                    color = SubText,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(partySize) },
                colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                enabled = partySize <= spotsLeft
            ) {
                Text("Book $partySize spots", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SubText) }
        }
    )
}
