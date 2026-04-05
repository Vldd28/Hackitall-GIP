package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.Event
import org.example.project.data.repository.RecommendationRepository

class RecommendationViewModel(
    private val repository: RecommendationRepository = RecommendationRepository()
) : ViewModel() {

    private val _recommendedEvents = MutableStateFlow<List<Event>>(emptyList())
    val recommendedEvents = _recommendedEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadRecommendations(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching { repository.getRecommendedEvents(userId) }
                .onSuccess { _recommendedEvents.value = it }
                .onFailure { _error.value = it.message ?: "Failed to load recommendations" }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
