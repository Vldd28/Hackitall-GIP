package org.example.project.data.model

enum class PlaceType(val label: String, val googleType: String) {
    MUSEUM("Museums", "museum"),
    CAFE("Cafés", "cafe"),
    CLUB("Clubs", "night_club"),
    RESTAURANT("Restaurants", "restaurant")
}

data class PlaceResult(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val type: PlaceType,
    val address: String = "",
    val rating: Double? = null,
    val totalRatings: Int? = null,
    val photoNames: List<String> = emptyList()
)
