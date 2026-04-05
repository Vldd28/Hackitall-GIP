package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.Event
import org.example.project.data.model.EventPhoto
import org.example.project.data.repository.EventPhotoRepository
import kotlin.random.Random

class SelfieViewModel(
    private val repository: EventPhotoRepository = EventPhotoRepository()
) : ViewModel() {

    /** The event currently active for selfie prompt. Null = no active event. */
    private val _activeEvent = MutableStateFlow<Event?>(null)
    val activeEvent = _activeEvent.asStateFlow()

    /** Whether the selfie prompt dialog should be shown. */
    private val _showSelfiePrompt = MutableStateFlow(false)
    val showSelfiePrompt = _showSelfiePrompt.asStateFlow()

    /** Loading state during upload. */
    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    /** User's event photos for profile display. */
    private val _userPhotos = MutableStateFlow<List<EventPhoto>>(emptyList())
    val userPhotos = _userPhotos.asStateFlow()

    /** All events the user joined (for associating photos with event names). */
    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents = _userEvents.asStateFlow()

    private var pollingJob: Job? = null
    private var promptTimerJob: Job? = null

    /**
     * Start polling for active events. Call this when user logs in.
     * Checks every 60 seconds if user is in an active event.
     * When an active event is found, starts a random selfie timer.
     */
    fun startMonitoring(userId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                runCatching {
                    val activeEvents = repository.getActiveEventsForUser(userId)
                    val newActive = activeEvents.firstOrNull()

                    if (newActive != null && _activeEvent.value?.id != newActive.id) {
                        // New active event detected — start selfie timer
                        _activeEvent.value = newActive
                        startSelfieTimer()
                    } else if (newActive == null) {
                        _activeEvent.value = null
                        promptTimerJob?.cancel()
                    }
                }
                delay(60_000L) // Check every 60 seconds
            }
        }
    }

    /**
     * Schedule a selfie prompt at a random time (2-10 minutes from now).
     */
    private fun startSelfieTimer() {
        promptTimerJob?.cancel()
        promptTimerJob = viewModelScope.launch {
            val delayMinutes = Random.nextInt(2, 11) // 2 to 10 minutes
            delay(delayMinutes * 60_000L)

            // Only show if still in an active event
            if (_activeEvent.value != null) {
                _showSelfiePrompt.value = true
            }
        }
    }

    /** User dismissed the selfie prompt. Schedule another one. */
    fun dismissPrompt() {
        _showSelfiePrompt.value = false
        // Schedule another prompt later
        if (_activeEvent.value != null) {
            startSelfieTimer()
        }
    }

    /** Upload the captured selfie. */
    fun uploadSelfie(userId: String, photoBytes: ByteArray) {
        val event = _activeEvent.value ?: return
        _isUploading.value = true
        viewModelScope.launch {
            runCatching {
                repository.uploadEventPhoto(
                    eventId = event.id,
                    userId = userId,
                    photoBytes = photoBytes,
                    caption = "Selfie at ${event.title}"
                )
            }.onSuccess {
                _showSelfiePrompt.value = false
                _isUploading.value = false
                // Refresh user photos
                loadUserPhotos(userId)
                // Schedule next prompt
                startSelfieTimer()
            }.onFailure {
                _isUploading.value = false
                // Still dismiss prompt on failure to avoid getting stuck
                _showSelfiePrompt.value = false
                startSelfieTimer()
            }
        }
    }

    /** Load all photos by this user (for profile display). */
    fun loadUserPhotos(userId: String) {
        viewModelScope.launch {
            runCatching {
                val photos = repository.getUserPhotos(userId)
                println("DEBUG: Loaded ${photos.size} photos for user $userId")
                photos.forEach { photo ->
                    println("DEBUG: Photo - eventId=${photo.eventId}, path=${photo.storagePath}")
                }
                _userPhotos.value = photos
            }.onFailure { error ->
                println("ERROR: Failed to load user photos: ${error.message}")
                error.printStackTrace()
            }
        }
    }

    /** Load events user joined (to map photo -> event title). */
    fun loadUserEvents(userId: String) {
        viewModelScope.launch {
            runCatching {
                val eventRepo = org.example.project.data.repository.EventRepository()
                val events = eventRepo.getUserJoinedEvents(userId)
                println("DEBUG: Loaded ${events.size} joined events for user $userId")
                events.forEach { event ->
                    println("DEBUG: Event - id=${event.id}, title=${event.title}")
                }
                _userEvents.value = events
            }.onFailure { error ->
                println("ERROR: Failed to load user events: ${error.message}")
                error.printStackTrace()
            }
        }
    }

    fun stopMonitoring() {
        pollingJob?.cancel()
        promptTimerJob?.cancel()
        _activeEvent.value = null
        _showSelfiePrompt.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
