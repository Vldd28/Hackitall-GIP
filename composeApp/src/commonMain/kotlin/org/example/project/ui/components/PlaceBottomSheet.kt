package org.example.project.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.data.model.Event
import org.example.project.data.model.EventInsert
import org.example.project.data.model.EventParticipant
import org.example.project.data.model.PlaceResult
import org.example.project.data.model.PlaceType
import org.example.project.data.remote.PlacesConfig
import org.example.project.data.repository.EventRepository
import kotlin.math.*
import kotlin.random.Random

// Light theme palette
private val SheetBg     = Color(0xFFF5FBF6)
private val SheetCard   = Color(0xFFE4F4E8)
private val SheetChip   = Color(0xFFD2EDD8)
private val SheetTeal   = Color(0xFF4A9E8E)
private val SheetGreen  = Color(0xFF5BAD72)
private val SheetText   = Color(0xFF2A4A3A)
private val SheetSub    = Color(0xFF7A9A8A)
private val SheetBorder = Color(0xFFB8DEC0)
private val SheetRed    = Color(0xFFE05252)
private val StarColor   = Color(0xFFFFD700)
private val DarkBg      = Color(0xFF31363F)  // unused, kept for compat
private val SubText     = Color(0xFFAAAAAA)  // unused, kept for compat

private fun Int.pad2() = toString().padStart(2, '0')
private fun Int.pad4() = toString().padStart(4, '0')
private fun nowIso(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.year.pad4()}-${now.monthNumber.pad2()}-${now.dayOfMonth.pad2()}T${now.hour.pad2()}:${now.minute.pad2()}:${now.second.pad2()}"
}
private fun Double.toOneDecimal(): String {
    val rounded = kotlin.math.round(this * 10).toInt()
    return "${rounded / 10}.${rounded % 10}"
}

private fun placeDistanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6371000.0
    val dLat = PI / 180.0 * (lat2 - lat1)
    val dLon = PI / 180.0 * (lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(PI / 180.0 * lat1) * cos(PI / 180.0 * lat2) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun eventsAtPlace(place: PlaceResult, allEvents: List<Event>): List<Event> =
    allEvents.filter { it.locationName == place.name }

@Composable
private fun StarRow(rating: Double) {
    val full  = rating.toInt()
    val half  = if (rating - full >= 0.5) 1 else 0
    val empty = 5 - full - half
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(full)  { Icon(Icons.Default.Star, null, tint = StarColor, modifier = Modifier.size(16.dp)) }
        repeat(half)  { Icon(Icons.Default.Star, null, tint = StarColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp)) }
        repeat(empty) { Icon(Icons.Default.Star, null, tint = SheetSub, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(4.dp))
        Text(rating.toOneDecimal(), color = SheetText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Particle explosion overlay ────────────────────────────────────────────────
private data class Particle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val color: Color,
    val radius: Float
)

@Composable
fun ParticleExplosion(onFinished: () -> Unit) {
    val particleColors = listOf(
        Color(0xFF5BAD72), Color(0xFF4A9E8E), Color(0xFFB4DEBD),
        Color(0xFF80A1BA), Color(0xFFFFD700), Color(0xFFFF8C69),
        Color(0xFF91C4C3), Color(0xFFD2EDD8)
    )
    val particles = remember {
        List(60) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 18f + 6f
            Particle(
                x = 0.5f, y = 0.5f,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                color = particleColors[it % particleColors.size],
                radius = Random.nextFloat() * 8f + 4f
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(900, easing = LinearEasing))
        onFinished()
    }
    val p = progress.value
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        particles.forEach { particle ->
            val alpha = (1f - p).coerceIn(0f, 1f)
            val px = (particle.x + particle.vx * p / 100f) * w
            val py = (particle.y + particle.vy * p / 100f + 0.5f * 9.8f * (p / 100f) * (p / 100f) * 10000f) * h
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.radius * (1f - p * 0.5f),
                center = Offset(px, py)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceBottomSheet(
    place: PlaceResult,
    allEvents: List<Event>,
    userId: String,
    onDismiss: () -> Unit
) {
    val apiKey = PlacesConfig.API_KEY
    val eventsHere = remember(place, allEvents) { eventsAtPlace(place, allEvents) }
    var showCreateForm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SheetBg,
        contentColor = SheetText,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SheetBorder)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            if (place.photoNames.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(place.photoNames) { photoName ->
                            val url = "https://places.googleapis.com/v1/$photoName/media?maxWidthPx=400&key=$apiKey"
                            AsyncImage(
                                model = url,
                                contentDescription = place.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 200.dp, height = 140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SheetChip)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when (place.type) {
                                PlaceType.MUSEUM     -> "🏛 Museum"
                                PlaceType.CAFE       -> "☕ Café"
                                PlaceType.CLUB       -> "🎵 Club"
                                PlaceType.RESTAURANT -> "🍴 Restaurant"
                            },
                            color = SheetTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(place.name, color = SheetText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    if (place.rating != null) {
                        StarRow(place.rating)
                        place.totalRatings?.let { Text("$it reviews", color = SheetSub, fontSize = 12.sp) }
                        Spacer(Modifier.height(4.dp))
                    }
                    if (place.address.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = SheetSub, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(place.address, color = SheetSub, fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    color = SheetBorder
                )
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Events here", color = SheetText, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showCreateForm = true }) {
                        Icon(Icons.Default.Add, null, tint = SheetGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Create event", color = SheetGreen, fontSize = 13.sp)
                    }
                }
            }

            if (eventsHere.isEmpty()) {
                item {
                    Text("No events yet at this location.", color = SheetSub, fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            } else {
                items(eventsHere) { event -> PlaceEventCard(event, userId) }
            }
        }
    }

    if (showCreateForm) {
        CreateEventDialog(place = place, userId = userId, onDismiss = { showCreateForm = false })
    }
}

@Composable
private fun PlaceEventCard(event: Event, userId: String) {
    val repo = remember { EventRepository() }
    val scope = rememberCoroutineScope()
    val now = remember { nowIso() }

    val parts = event.dateTime.split("T")
    val datePart = parts.getOrNull(0) ?: event.dateTime
    val timePart = parts.getOrNull(1)?.take(5) ?: ""
    val isUpcoming = event.dateTime >= now

    var participants by remember { mutableStateOf<List<EventParticipant>>(emptyList()) }
    var hasJoined by remember { mutableStateOf(false) }

    LaunchedEffect(event.id) {
        runCatching {
            participants = repo.getEventParticipants(event.id)
            hasJoined = participants.any { it.profileId == userId }
        }
    }

    val spotsLeft = event.maxParticipants - participants.size
    val isFull = spotsLeft <= 0
    val spotsText = if (participants.isEmpty()) "${event.maxParticipants} spots total" else "$spotsLeft / ${event.maxParticipants} spots left"

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SheetCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(event.title, color = SheetText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = SheetTeal, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(datePart, color = SheetSub, fontSize = 12.sp)
                }
                if (timePart.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = SheetTeal, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(timePart, color = SheetSub, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SheetChip)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(spotsText, color = SheetTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            if (isUpcoming) {
                Spacer(Modifier.height(8.dp))
                when {
                    hasJoined -> Text("✓ Joined", color = SheetTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    isFull    -> Text("Full", color = SheetRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    else -> Button(
                        onClick = {
                            scope.launch {
                                runCatching {
                                    repo.joinEvent(event.id, userId)
                                    participants = repo.getEventParticipants(event.id)
                                    hasJoined = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SheetTeal),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Join", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventDialog(place: PlaceResult, userId: String, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("10") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds()
    )
    val timePickerState = rememberTimePickerState(initialHour = 20, initialMinute = 0)
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val selectedDate = datePickerState.selectedDateMillis
    val dateLabel = if (selectedDate != null) {
        val local = Instant.fromEpochMilliseconds(selectedDate).toLocalDateTime(TimeZone.UTC)
        "${local.year.pad4()}-${local.monthNumber.pad2()}-${local.dayOfMonth.pad2()}"
    } else "Pick a date"

    val timeLabel = "${timePickerState.hour.pad2()}:${timePickerState.minute.pad2()}"

    val repo = remember { EventRepository() }
    val scope = rememberCoroutineScope()

    var showParticles by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AlertDialog(
            onDismissRequest = { if (!showParticles) onDismiss() },
            containerColor = SheetBg,
            titleContentColor = SheetText,
            textContentColor = SheetText,
            title = { Text("New Event at ${place.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SheetText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it },
                        label = { Text("Title") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), colors = lightTextFieldColors())
                    OutlinedTextField(value = description, onValueChange = { description = it },
                        label = { Text("Description (optional)") }, maxLines = 3,
                        modifier = Modifier.fillMaxWidth(), colors = lightTextFieldColors())

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SheetBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SheetText)
                    ) {
                        Icon(Icons.Default.DateRange, null, tint = SheetTeal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(dateLabel, color = if (selectedDate != null) SheetText else SheetSub, fontSize = 14.sp)
                        Spacer(Modifier.weight(1f))
                    }

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SheetBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SheetText)
                    ) {
                        Icon(Icons.Default.AccessTime, null, tint = SheetTeal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(timeLabel, color = SheetText, fontSize = 14.sp)
                        Spacer(Modifier.weight(1f))
                    }

                    OutlinedTextField(value = maxParticipants, onValueChange = { maxParticipants = it },
                        label = { Text("Max participants") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), colors = lightTextFieldColors())
                    error?.let { Text(it, color = Color(0xFFD9534F), fontSize = 12.sp) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isBlank() || selectedDate == null) {
                            error = "Title and date are required."
                            return@Button
                        }
                        isLoading = true; error = null
                        val insert = EventInsert(
                            creatorId = userId, title = title.trim(),
                            description = description.trim().ifBlank { null },
                            locationName = place.name, lat = place.lat, lng = place.lng,
                            dateTime = "${dateLabel}T${timeLabel}:00",
                            maxParticipants = maxParticipants.toIntOrNull() ?: 10, isPublic = true
                        )
                        scope.launch {
                            runCatching { repo.createEvent(insert) }
                                .onSuccess { showParticles = true }
                                .onFailure { error = it.message ?: "Failed to create event." }
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SheetGreen),
                    enabled = !isLoading && !showParticles
                ) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Create", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = SheetSub) } }
        )

        if (showParticles) {
            ParticleExplosion(onFinished = { showParticles = false; onDismiss() })
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK", color = SheetGreen, fontWeight = FontWeight.SemiBold) } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = SheetSub) } },
            colors = DatePickerDefaults.colors(containerColor = SheetBg)
        ) {
            DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(
                containerColor = SheetBg, titleContentColor = SheetTeal,
                headlineContentColor = SheetText, weekdayContentColor = SheetSub,
                selectedDayContainerColor = SheetGreen, selectedDayContentColor = Color.White,
                todayContentColor = SheetGreen, todayDateBorderColor = SheetGreen,
                dayContentColor = SheetText
            ))
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = SheetBg,
            title = { Text("Select time", color = SheetText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState, colors = TimePickerDefaults.colors(
                        clockDialColor = SheetCard, clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = SheetText, selectorColor = SheetGreen,
                        containerColor = SheetBg, timeSelectorSelectedContainerColor = SheetGreen,
                        timeSelectorUnselectedContainerColor = SheetCard,
                        timeSelectorSelectedContentColor = Color.White, timeSelectorUnselectedContentColor = SheetText
                    ))
                }
            },
            confirmButton = { TextButton(onClick = { showTimePicker = false }) { Text("OK", color = SheetGreen, fontWeight = FontWeight.SemiBold) } },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = SheetSub) } }
        )
    }
}

@Composable
private fun lightTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SheetGreen, unfocusedBorderColor = SheetBorder,
    focusedLabelColor = SheetGreen, unfocusedLabelColor = SheetSub,
    focusedTextColor = SheetText, unfocusedTextColor = SheetText, cursorColor = SheetGreen
)
