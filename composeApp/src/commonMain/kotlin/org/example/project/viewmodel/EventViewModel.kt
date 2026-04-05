package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.Event
import org.example.project.data.model.EventInsert
import org.example.project.data.model.EventParticipant
import org.example.project.data.repository.EventRepository

class EventViewModel(
    private val repository: EventRepository = EventRepository()
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent = _selectedEvent.asStateFlow()

    private val _participants = MutableStateFlow<List<EventParticipant>>(emptyList())
    val participants = _participants.asStateFlow()

    private val _userCreatedEvents = MutableStateFlow<List<Event>>(emptyList())
    val userCreatedEvents = _userCreatedEvents.asStateFlow()

    private val _userJoinedEvents = MutableStateFlow<List<Event>>(emptyList())
    val userJoinedEvents = _userJoinedEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadPublicEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { _events.value = repository.getPublicEvents() }
                .onFailure { _error.value = it.message ?: "Failed to load events" }
            _isLoading.value = false
        }
    }

    fun loadEventById(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { _selectedEvent.value = repository.getEventById(eventId) }
                .onFailure { _error.value = it.message ?: "Failed to load event" }
            _isLoading.value = false
        }
    }

    fun loadParticipants(eventId: String) {
        viewModelScope.launch {
            runCatching { _participants.value = repository.getEventParticipants(eventId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun createEvent(insert: EventInsert, onSuccess: (Event) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { repository.createEvent(insert) }
                .onSuccess { event ->
                    _events.value = _events.value + event
                    onSuccess(event)
                }
                .onFailure { _error.value = it.message ?: "Failed to create event" }
            _isLoading.value = false
        }
    }

    fun joinEvent(eventId: String, userId: String) {
        viewModelScope.launch {
            runCatching { repository.joinEvent(eventId, userId) }
                .onSuccess { loadParticipants(eventId) }
                .onFailure { _error.value = it.message ?: "Failed to join event" }
        }
    }

    fun leaveEvent(eventId: String, userId: String) {
        viewModelScope.launch {
            runCatching { repository.leaveEvent(eventId, userId) }
                .onSuccess { loadParticipants(eventId) }
                .onFailure { _error.value = it.message ?: "Failed to leave event" }
        }
    }

    fun loadUserCreatedEvents(userId: String) {
        viewModelScope.launch {
            runCatching { _userCreatedEvents.value = repository.getUserCreatedEvents(userId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun loadUserJoinedEvents(userId: String) {
        viewModelScope.launch {
            runCatching { _userJoinedEvents.value = repository.getUserJoinedEvents(userId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun addEvent(event: Event) {
        _events.value = _events.value + event
    }

    fun clearError() { _error.value = null }
}
