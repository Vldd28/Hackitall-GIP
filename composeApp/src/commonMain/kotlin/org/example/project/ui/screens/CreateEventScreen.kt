package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.example.project.ui.components.InterestChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit,
    onCreateClick: (EventFormData) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("10") }
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    
    val availableInterests = listOf(
        "Art", "Music", "Sports", "Food", "Tech", 
        "Nature", "Culture", "Nightlife", "Photography", "Hiking"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Event") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Event Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title *") },
                placeholder = { Text("e.g., Sunset Hike at Mount Peak") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Tell people what to expect...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
            
            // Location
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location *") },
                placeholder = { Text("e.g., Central Park, Amsterdam") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // TODO: Add map picker for lat/lng
            Text(
                text = "💡 Tap on map to select exact location (coming soon)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Date & Time
            OutlinedTextField(
                value = dateTime,
                onValueChange = { dateTime = it },
                label = { Text("Date & Time *") },
                placeholder = { Text("2024-04-04T14:30:00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Text(
                text = "💡 Format: YYYY-MM-DDTHH:MM:SS",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Max Participants
            OutlinedTextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it.filter { char -> char.isDigit() } },
                label = { Text("Max Participants") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Interests Section
            Text(
                text = "Select Interests *",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Choose activities that match this event",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Interest chips grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableInterests.chunked(3).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { interest ->
                            InterestChip(
                                interestName = interest,
                                selected = interest in selectedInterests,
                                onToggle = {
                                    selectedInterests = if (interest in selectedInterests) {
                                        selectedInterests - interest
                                    } else {
                                        selectedInterests + interest
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Create Button
            Button(
                onClick = {
                    onCreateClick(
                        EventFormData(
                            title = title,
                            description = description.ifBlank { null },
                            locationName = locationName,
                            dateTime = dateTime,
                            maxParticipants = maxParticipants.toIntOrNull() ?: 10,
                            selectedInterests = selectedInterests.toList()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = title.isNotBlank() && 
                         locationName.isNotBlank() && 
                         dateTime.isNotBlank() &&
                         selectedInterests.isNotEmpty()
            ) {
                Text("Create Event")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

data class EventFormData(
    val title: String,
    val description: String?,
    val locationName: String,
    val dateTime: String,
    val maxParticipants: Int,
    val selectedInterests: List<String>
)
