package org.example.project.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.example.project.data.model.EventPhoto
import org.example.project.data.model.EventPhotoInsert
import org.example.project.data.model.Event
import org.example.project.data.model.EventParticipant
import org.example.project.data.remote.supabase

class EventPhotoRepository {

    /**
     * Returns events the user is participating in that are currently active.
     * "Active" = event started within the last 3 hours (no explicit end time in schema).
     */
    suspend fun getActiveEventsForUser(userId: String): List<Event> {
        println("DEBUG EventPhotoRepo: getActiveEventsForUser called for userId=$userId")
        
        // Get all event IDs the user has joined
        val joined = supabase.from("event_participants").select {
            filter { eq("profile_id", userId) }
        }.decodeList<EventParticipant>()
        
        println("DEBUG EventPhotoRepo: User has joined ${joined.size} events")
        joined.forEach { p ->
            println("DEBUG EventPhotoRepo: Participant - eventId=${p.eventId}, status=${p.status}")
        }

        if (joined.isEmpty()) {
            println("DEBUG EventPhotoRepo: ❌ No joined events found!")
            return emptyList()
        }

        val joinedIds = joined.map { it.eventId }.toSet()

        // Get all public events and filter to active ones the user joined
        val allEvents = supabase.from("events").select().decodeList<Event>()
        println("DEBUG EventPhotoRepo: Total events in database: ${allEvents.size}")

        val activeEvents = allEvents.filter { event ->
            val isJoined = event.id in joinedIds
            val isActive = isEventActive(event.dateTime)
            println("DEBUG EventPhotoRepo: Event '${event.title}' - joined=$isJoined, active=$isActive, dateTime=${event.dateTime}")
            isJoined && isActive
        }
        
        println("DEBUG EventPhotoRepo: ✅ Returning ${activeEvents.size} active events")
        return activeEvents
    }

    /**
     * Encode photo as base64 data URI and insert a record into event_photos.
     * No storage bucket required — the image data is stored directly in the DB.
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun uploadEventPhoto(
        eventId: String,
        userId: String,
        photoBytes: ByteArray,
        caption: String? = "Selfie moment!"
    ): EventPhoto {
        // Encode photo bytes as a base64 data URI so it can be stored in the DB
        // and loaded directly by Coil's AsyncImage without any storage bucket.
        val base64 = Base64.encode(photoBytes)
        val dataUri = "data:image/jpeg;base64,$base64"

        val insert = EventPhotoInsert(
            eventId = eventId,
            uploadedBy = userId,
            storagePath = dataUri,
            caption = caption
        )

        println("DEBUG EventPhotoRepo: Inserting photo record for eventId=$eventId userId=$userId")
        val result = supabase.from("event_photos").insert(insert) {
            select()
        }.decodeSingle<EventPhoto>()
        println("DEBUG EventPhotoRepo: ✅ Photo inserted with id=${result.id}")
        return result
    }

    /**
     * Get all photos uploaded by a specific user, ordered by newest first.
     */
    suspend fun getUserPhotos(userId: String): List<EventPhoto> =
        supabase.from("event_photos").select {
            filter { eq("uploaded_by", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    /**
     * Get all photos for a specific event.
     */
    suspend fun getEventPhotos(eventId: String): List<EventPhoto> =
        supabase.from("event_photos").select {
            filter { eq("event_id", eventId) }
        }.decodeList()

    /**
     * Get photos grouped with their event info for a user's profile display.
     * Uses the profile_past_events view.
     */
    suspend fun getUserPastEventsWithPhotos(userId: String): List<EventPhoto> {
        // Fetch all photos by user, then we can group them client-side with event info
        return supabase.from("event_photos").select {
            filter { eq("uploaded_by", userId) }
        }.decodeList()
    }

    companion object {
        /**
         * Check if an event is currently active based on its dateTime string.
         * TESTING MODE: Event is active as soon as it has started (no time limit).
         * Production: should check if started within last 3 hours.
         */
        fun isEventActive(dateTimeStr: String): Boolean {
            return try {
                val now = kotlinx.datetime.Clock.System.now()
                println("DEBUG isEventActive: Current time = $now")
                println("DEBUG isEventActive: Event dateTime string = '$dateTimeStr'")
                
                // Parse ISO datetime (handle various formats)
                val cleaned = dateTimeStr
                    .replace(" ", "T")
                    .let { if (it.contains("+") || it.endsWith("Z")) it else "${it}Z" }
                println("DEBUG isEventActive: Cleaned dateTime = '$cleaned'")
                
                val eventInstant = kotlinx.datetime.Instant.parse(cleaned)
                println("DEBUG isEventActive: Parsed instant = $eventInstant")

                val diffMs = (now - eventInstant).inWholeMilliseconds
                println("DEBUG isEventActive: Time difference = $diffMs ms (${diffMs/1000} seconds)")
                
                val isActive = diffMs >= 0
                println("DEBUG isEventActive: Result = $isActive (event has started: ${diffMs >= 0})")
                
                // TESTING: Event is active if it has started (diffMs > 0), no upper limit
                isActive
                
                // PRODUCTION (restore later): diffMs in 0..10_800_000L (3 hours)
            } catch (e: Exception) {
                println("ERROR isEventActive: Failed to parse dateTime: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }
}
