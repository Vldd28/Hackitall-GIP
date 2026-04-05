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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.data.model.Event
import org.example.project.data.model.EventParticipant
import org.example.project.data.repository.EventRepository
import kotlin.math.*

private val EventSheetDarkBg    = Color(0xFF31363F)
private val EventSheetTealColor = Color(0xFF76ABAE)
private val EventSheetLightText = Color(0xFFEEEEEE)
private val EventSheetSubText   = Color(0xFFAAAAAA)
private val EventSheetCardBg    = Color(0xFF3A3F47)
private val EventSheetRedColor  = Color(0xFFE8534A)

private fun eventDistMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6371000.0
    val dLat = PI / 180.0 * (lat2 - lat1)
    val dLon = PI / 180.0 * (lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(PI / 180.0 * lat1) * cos(PI / 180.0 * lat2) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun Int.pad2() = toString().padStart(2, '0')
private fun Int.pad4() = toString().padStart(4, '0')

private fun nowIso(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.year.pad4()}-${now.monthNumber.pad2()}-${now.dayOfMonth.pad2()}T${now.hour.pad2()}:${now.minute.pad2()}:${now.second.pad2()}"
}

private fun formatDateTime(iso: String): String = try {
    val parts = iso.split("T")
    val dateParts = parts[0].split("-")
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val month = months.getOrElse(dateParts[1].toInt() - 1) { dateParts[1] }
    val time = parts.getOrElse(1) { "" }.take(5)
    "${dateParts[2]} $month ${dateParts[0]} · $time"
} catch (e: Exception) { iso }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBottomSheet(
    tappedEvent: Event,
    allEvents: List<Event>,
    userId: String,
    onDismiss: () -> Unit
) {
    val now = remember { nowIso() }

    val locationEvents = remember(tappedEvent, allEvents) {
        allEvents.filter { it.locationName == tappedEvent.locationName }
    }
    val upcomingEvents = remember(locationEvents) {
        locationEvents.filter { it.dateTime >= now }.sortedBy { it.dateTime }
    }
    val pastEvents = remember(locationEvents) {
        locationEvents.filter { it.dateTime < now }.sortedByDescending { it.dateTime }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = EventSheetDarkBg,
        contentColor = EventSheetLightText,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(EventSheetSubText)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(tappedEvent.locationName, color = EventSheetTealColor,
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (upcomingEvents.isNotEmpty()) {
                item { EventSectionHeader("Upcoming Events") }
                items(upcomingEvents) { event ->
                    LocationEventCard(event = event, userId = userId, isUpcoming = true)
                }
            }

            if (pastEvents.isNotEmpty()) {
                item { EventSectionHeader("Past Events") }
                items(pastEvents) { event ->
                    LocationEventCard(event = event, userId = userId, isUpcoming = false)
                }
            }

            if (locationEvents.isEmpty()) {
                item {
                    Text("No events at this location yet.", color = EventSheetSubText,
                        fontSize = 13.sp, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EventSectionHeader(title: String) {
    Text(text = title, color = EventSheetLightText, fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
}

@Composable
private fun LocationEventCard(event: Event, userId: String, isUpcoming: Boolean) {
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = EventSheetCardBg)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
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
                Text(event.title, color = EventSheetLightText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(formatDateTime(event.dateTime), color = EventSheetTealColor, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))

                EventLobbyRow(current = participants.size, max = event.maxParticipants)

                if (isUpcoming) {
                    Spacer(Modifier.height(8.dp))
                    when {
                        hasJoined -> Text("✓ Joined", color = EventSheetTealColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        isFull    -> Text("Full", color = EventSheetRedColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                colors = ButtonDefaults.buttonColors(containerColor = EventSheetTealColor),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Join", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showPartyDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EventSheetTealColor),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EventSheetTealColor),
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
        EventPartyJoinDialog(
            event = event, spotsLeft = spotsLeft, userId = userId,
            onConfirm = { _ ->
                showPartyDialog = false
                scope.launch {
                    runCatching {
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
private fun EventLobbyRow(current: Int, max: Int) {
    val fraction = if (max > 0) current.toFloat() / max else 0f
    val barColor = when {
        fraction >= 1f    -> EventSheetRedColor
        fraction >= 0.75f -> Color(0xFFFFAA44)
        else              -> EventSheetTealColor
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, null, tint = EventSheetSubText, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text("$current / $max spots", color = EventSheetSubText, fontSize = 12.sp)
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
private fun EventPartyJoinDialog(
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
        containerColor = EventSheetDarkBg,
        titleContentColor = EventSheetLightText,
        textContentColor = EventSheetLightText,
        title = { Text("Join as Party", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("How many people in your group?", color = EventSheetSubText, fontSize = 13.sp)
                Text("$partySize people", color = EventSheetTealColor, fontSize = 28.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Slider(
                    value = partySize.toFloat(),
                    onValueChange = { partySize = it.toInt() },
                    valueRange = 2f..maxParty.toFloat().coerceAtLeast(2f),
                    steps = maxOf(0, maxParty - 3),
                    colors = SliderDefaults.colors(
                        thumbColor = EventSheetTealColor,
                        activeTrackColor = EventSheetTealColor,
                        inactiveTrackColor = EventSheetCardBg
                    )
                )
                Text("$spotsLeft spots available", color = EventSheetSubText, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(partySize) },
                colors = ButtonDefaults.buttonColors(containerColor = EventSheetTealColor),
                enabled = partySize <= spotsLeft
            ) { Text("Book $partySize spots", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = EventSheetSubText) } }
    )
}
