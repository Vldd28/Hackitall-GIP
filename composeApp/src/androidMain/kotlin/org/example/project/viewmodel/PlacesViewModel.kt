package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.PlaceResult
import org.example.project.data.model.PlaceType
import org.example.project.data.repository.PlacesRepository

class PlacesViewModel(private val repository: PlacesRepository) : ViewModel() {

    private val _places = MutableStateFlow<List<PlaceResult>>(emptyList())
    val places = _places.asStateFlow()

    private val allTypes = setOf(PlaceType.RESTAURANT, PlaceType.CAFE, PlaceType.MUSEUM, PlaceType.CLUB)

    private var searchJob: Job? = null

    /** Call when camera stops moving. Debounces rapid calls by 400ms. */
    fun loadPlaces(lat: Double, lng: Double, radius: Double = 1500.0) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _places.value = repository.searchNearby(lat, lng, allTypes, radius)
        }
    }
}
