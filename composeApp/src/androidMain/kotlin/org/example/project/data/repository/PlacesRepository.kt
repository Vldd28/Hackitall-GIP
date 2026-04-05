package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.data.model.PlaceResult
import org.example.project.data.model.PlaceType

class PlacesRepository(private val apiKey: String) {

    private val client = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun searchNearby(
        lat: Double,
        lng: Double,
        types: Set<PlaceType>,
        radiusMeters: Double = 1500.0
    ): List<PlaceResult> {
        if (apiKey.isBlank() || types.isEmpty()) return emptyList()

        val typeList = types.joinToString(",") { "\"${it.googleType}\"" }
        val body = """
        {
          "locationRestriction": {
            "circle": {
              "center": { "latitude": $lat, "longitude": $lng },
              "radius": $radiusMeters
            }
          },
          "includedTypes": [$typeList],
          "maxResultCount": 20
        }
        """.trimIndent()

        return try {
            val response = client.post("https://places.googleapis.com/v1/places:searchNearby") {
                headers {
                    append("Content-Type", "application/json")
                    append("X-Goog-Api-Key", apiKey)
                    append("X-Goog-FieldMask",
                        "places.id,places.displayName,places.location,places.formattedAddress,places.types,places.rating,places.userRatingCount,places.photos")
                }
                setBody(body)
            }
            val text = response.bodyAsText()
            android.util.Log.d("PlacesRepo", "Status: ${response.status}, Body: $text")
            parsePlaces(text, types)
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepo", "Request failed: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun searchByText(query: String): List<PlaceResult> {
        if (apiKey.isBlank() || query.isBlank()) return emptyList()
        val body = """{"textQuery": "${query.replace("\"", "\\\"")}","maxResultCount": 5}"""
        return try {
            val response = client.post("https://places.googleapis.com/v1/places:searchText") {
                headers {
                    append("Content-Type", "application/json")
                    append("X-Goog-Api-Key", apiKey)
                    append("X-Goog-FieldMask",
                        "places.id,places.displayName,places.location,places.formattedAddress,places.types,places.rating,places.userRatingCount,places.photos")
                }
                setBody(body)
            }
            val text = response.bodyAsText()
            android.util.Log.d("PlacesRepo", "TextSearch: ${response.status}, Body: $text")
            parseTextSearchPlaces(text)
        } catch (e: Exception) {
            android.util.Log.e("PlacesRepo", "TextSearch failed: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseTextSearchPlaces(responseText: String): List<PlaceResult> {
        val root = json.parseToJsonElement(responseText).jsonObject
        val placesArray = root["places"]?.jsonArray ?: return emptyList()
        return placesArray.mapNotNull { elem ->
            try {
                val obj = elem.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val name = obj["displayName"]?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""
                val loc = obj["location"]?.jsonObject ?: return@mapNotNull null
                val lat = loc["latitude"]?.jsonPrimitive?.double ?: return@mapNotNull null
                val lng = loc["longitude"]?.jsonPrimitive?.double ?: return@mapNotNull null
                val address = obj["formattedAddress"]?.jsonPrimitive?.content ?: ""
                val rating = obj["rating"]?.jsonPrimitive?.doubleOrNull
                val totalRatings = obj["userRatingCount"]?.jsonPrimitive?.intOrNull
                val apiTypes = obj["types"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                val placeType = PlaceType.entries.firstOrNull { it.googleType in apiTypes } ?: PlaceType.RESTAURANT
                val photoNames = obj["photos"]?.jsonArray?.take(5)
                    ?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content } ?: emptyList()
                PlaceResult(id = id, name = name, lat = lat, lng = lng,
                    type = placeType, address = address, rating = rating,
                    totalRatings = totalRatings, photoNames = photoNames)
            } catch (e: Exception) { null }
        }
    }

    private fun parsePlaces(responseText: String, requestedTypes: Set<PlaceType>): List<PlaceResult> {
        val root = json.parseToJsonElement(responseText).jsonObject
        val placesArray = root["places"]?.jsonArray ?: return emptyList()

        return placesArray.mapNotNull { elem ->
            try {
                val obj = elem.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val name = obj["displayName"]?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""
                val loc = obj["location"]?.jsonObject ?: return@mapNotNull null
                val lat = loc["latitude"]?.jsonPrimitive?.double ?: return@mapNotNull null
                val lng = loc["longitude"]?.jsonPrimitive?.double ?: return@mapNotNull null
                val address = obj["formattedAddress"]?.jsonPrimitive?.content ?: ""
                val rating = obj["rating"]?.jsonPrimitive?.doubleOrNull
                val totalRatings = obj["userRatingCount"]?.jsonPrimitive?.intOrNull

                val apiTypes = obj["types"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                val placeType = requestedTypes.firstOrNull { it.googleType in apiTypes }
                    ?: return@mapNotNull null

                val photoNames = obj["photos"]?.jsonArray
                    ?.take(5)
                    ?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content }
                    ?: emptyList()

                PlaceResult(
                    id = id, name = name, lat = lat, lng = lng,
                    type = placeType, address = address,
                    rating = rating, totalRatings = totalRatings,
                    photoNames = photoNames
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
