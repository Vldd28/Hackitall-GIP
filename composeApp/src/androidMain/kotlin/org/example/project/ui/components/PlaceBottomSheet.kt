package org.example.project.ui.components

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.data.model.Event
import org.example.project.data.model.EventInsert
import org.example.project.data.model.PlaceResult
import org.example.project.data.model.PlaceType
import org.example.project.data.repository.EventRepository
import kotlin.math.*

private val DarkBg     = Color(0xFF31363F)
private val TealColor  = Color(0xFF76ABAE)
private val LightText  = Color(0xFFEEEEEE)
private val SubText    = Color(0xFFAAAAAA)
private val CardBg     = Color(0xFF3A3F47)
private val StarColor  = Color(0xFFFFD700)

/** Returns approximate distance in metres between two lat/lng points. */
private fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
    return results[0].toDouble()
}

/** Events within 200m are considered "at this location". */
private fun eventsAtPlace(place: PlaceResult, allEvents: List<Event>): List<Event> =
    allEvents.filter { distanceMeters(it.lat, it.lng, place.lat, place.lng) <= 200.0 }

@Composable
private fun StarRow(rating: Double) {
    val full  = rating.toInt()
    val half  = if (rating - full >= 0.5) 1 else 0
    val empty = 5 - full - half
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(full)  { Icon(Icons.Default.Star, null, tint = StarColor, modifier = Modifier.size(16.dp)) }
        repeat(half)  { Icon(Icons.Default.Star, null, tint = StarColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp)) }
        repeat(empty) { Icon(Icons.Default.Star, null, tint = SubText, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(4.dp))
        Text("%.1f".format(rating), color = LightText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceBottomSheet(
    place: PlaceResult,
    allEvents: List<Event>,
    userId: String,
    apiKey: String,
    onDismiss: () -> Unit
) {
    val eventsHere = remember(place, allEvents) { eventsAtPlace(place, allEvents) }
    var showCreateForm by remember { mutableStateOf(false) }

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
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Photos ────────────────────────────────────────────────────────
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

            // ── Place info ────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Type chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when (place.type) {
                                PlaceType.MUSEUM -> "🏛 Museum"
                                PlaceType.CAFE -> "☕ Café"
                                PlaceType.CLUB -> "🎵 Club"
                                PlaceType.RESTAURANT -> "🍴 Restaurant"
                            },
                            color = TealColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))

                    Text(place.name, color = LightText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))

                    if (place.rating != null) {
                        StarRow(place.rating)
                        place.totalRatings?.let {
                            Text("$it reviews", color = SubText, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    if (place.address.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = SubText, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(place.address, color = SubText, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Divider ───────────────────────────────────────────────────────
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    color = CardBg
                )
            }

            // ── Events at this location ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Events here",
                        color = LightText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showCreateForm = true }) {
                        Icon(Icons.Default.Add, null, tint = TealColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Create event", color = TealColor, fontSize = 13.sp)
                    }
                }
            }

            if (eventsHere.isEmpty()) {
                item {
                    Text(
                        "No events yet at this location.",
                        color = SubText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(eventsHere) { event ->
                    EventCard(event)
                }
            }
        }
    }

    // ── Create Event form ─────────────────────────────────────────────────────
    if (showCreateForm) {
        CreateEventDialog(
            place = place,
            userId = userId,
            onDismiss = { showCreateForm = false }
        )
    }
}

@Composable
private fun EventCard(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(event.title, color = LightText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (!event.description.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(event.description, color = SubText, fontSize = 12.sp, maxLines = 2)
            }
            Spacer(Modifier.height(4.dp))
            Text(event.dateTime, color = TealColor, fontSize = 11.sp)
        }
    }
}

// ── Dark-themed color scheme for Material3 Date/Time pickers ─────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun wandrDatePickerColors() = DatePickerDefaults.colors(
    containerColor = DarkBg,
    titleContentColor = TealColor,
    headlineContentColor = LightText,
    weekdayContentColor = SubText,
    subheadContentColor = SubText,
    navigationContentColor = LightText,
    yearContentColor = LightText,
    currentYearContentColor = TealColor,
    selectedYearContainerColor = TealColor,
    selectedYearContentColor = DarkBg,
    dayContentColor = LightText,
    selectedDayContainerColor = TealColor,
    selectedDayContentColor = DarkBg,
    todayContentColor = TealColor,
    todayDateBorderColor = TealColor
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun wandrTimePickerColors() = TimePickerDefaults.colors(
    clockDialColor = CardBg,
    clockDialSelectedContentColor = DarkBg,
    clockDialUnselectedContentColor = LightText,
    selectorColor = TealColor,
    containerColor = DarkBg,
    periodSelectorBorderColor = SubText,
    periodSelectorSelectedContainerColor = TealColor,
    periodSelectorUnselectedContainerColor = DarkBg,
    periodSelectorSelectedContentColor = DarkBg,
    periodSelectorUnselectedContentColor = LightText,
    timeSelectorSelectedContainerColor = TealColor,
    timeSelectorUnselectedContainerColor = CardBg,
    timeSelectorSelectedContentColor = DarkBg,
    timeSelectorUnselectedContentColor = LightText
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventDialog(
    place: PlaceResult,
    userId: String,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("10") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Date picker state — default to today
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    // Time picker state — default to 20:00
    val timePickerState = rememberTimePickerState(initialHour = 20, initialMinute = 0)
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Format the selected date + time for display
    val selectedDate = datePickerState.selectedDateMillis
    val dateLabel = if (selectedDate != null) {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = selectedDate
        }
        "%04d-%02d-%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    } else "Pick a date"

    val timeLabel = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)

    val repo = remember { EventRepository() }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBg,
        titleContentColor = LightText,
        textContentColor = LightText,
        title = { Text("New Event at ${place.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = wandrTextFieldColors()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = wandrTextFieldColors()
                )

                // ── Date button ──────────────────────────────────────────
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubText),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText)
                ) {
                    Icon(Icons.Default.DateRange, null, tint = TealColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(dateLabel, color = if (selectedDate != null) LightText else SubText, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                }

                // ── Time button ──────────────────────────────────────────
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubText),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText)
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = TealColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(timeLabel, color = LightText, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = maxParticipants,
                    onValueChange = { maxParticipants = it },
                    label = { Text("Max participants") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = wandrTextFieldColors()
                )
                error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || selectedDate == null) {
                        error = "Title and date are required."
                        return@Button
                    }
                    isLoading = true
                    error = null
                    val dateTimeStr = "${dateLabel}T${timeLabel}:00"
                    val insert = EventInsert(
                        creatorId = userId,
                        title = title.trim(),
                        description = description.trim().ifBlank { null },
                        locationName = place.name,
                        lat = place.lat,
                        lng = place.lng,
                        dateTime = dateTimeStr,
                        maxParticipants = maxParticipants.toIntOrNull() ?: 10,
                        isPublic = true
                    )
                    scope.launch {
                        runCatching { repo.createEvent(insert) }
                            .onSuccess { onDismiss() }
                            .onFailure { error = it.message ?: "Failed to create event." }
                        isLoading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Create", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SubText) }
        }
    )

    // ── Date picker dialog ───────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK", color = TealColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = SubText)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = DarkBg)
        ) {
            DatePicker(
                state = datePickerState,
                colors = wandrDatePickerColors()
            )
        }
    }

    // ── Time picker dialog ───────────────────────────────────────────────────
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = DarkBg,
            title = {
                Text("Select time", color = LightText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = wandrTimePickerColors()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("OK", color = TealColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = SubText)
                }
            }
        )
    }
}

@Composable
private fun wandrTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TealColor,
    unfocusedBorderColor = SubText,
    focusedLabelColor = TealColor,
    unfocusedLabelColor = SubText,
    focusedTextColor = LightText,
    unfocusedTextColor = LightText,
    cursorColor = TealColor
)
