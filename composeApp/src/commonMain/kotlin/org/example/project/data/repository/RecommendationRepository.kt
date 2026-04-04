package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.data.model.Event
import org.example.project.data.remote.SupabaseConfig

class RecommendationRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            // ignoreUnknownKeys because the edge function may return extra fields
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getRecommendedEvents(userId: String): List<Event> =
        client.post("${SupabaseConfig.URL}/functions/v1/recommend-events") {
            headers {
                append("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
            }
            contentType(ContentType.Application.Json)
            setBody(RecommendRequest(userId))
        }.body()

    @Serializable
    private data class RecommendRequest(val userId: String)
}
