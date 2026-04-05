package org.example.project.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
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
        // Get all event IDs the user has joined
        val joined = supabase.from("event_participants").select {
            filter { eq("profile_id", userId) }
        }.decodeList<EventParticipant>()

        if (joined.isEmpty()) return emptyList()

        val joinedIds = joined.map { it.eventId }.toSet()

        // Get all public events and filter to active ones the user joined
        val allEvents = supabase.from("events").select().decodeList<Event>()

        return allEvents.filter { event ->
            event.id in joinedIds && isEventActive(event.dateTime)
        }
    }

    /**
     * Upload a selfie photo to Supabase Storage and insert a record into event_photos.
     */
    suspend fun uploadEventPhoto(
        eventId: String,
        userId: String,
        photoBytes: ByteArray,
        caption: String? = "Selfie moment!"
    ): EventPhoto {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val path = "$eventId/$userId/$timestamp.jpg"

        // Upload to storage bucket "event-photos"
        supabase.storage.from("event-photos").upload(path, photoBytes) {
            upsert = false
        }

        // Get public URL
        val publicUrl = supabase.storage.from("event-photos").publicUrl(path)

        // Insert record
        val insert = EventPhotoInsert(
            eventId = eventId,
            uploadedBy = userId,
            storagePath = publicUrl,
            caption = caption
        )

        return supabase.from("event_photos").insert(insert) {
            select()
        }.decodeSingle()
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
         * Active = started within the last 3 hours.
         */
        fun isEventActive(dateTimeStr: String): Boolean {
            return try {
                val now = kotlinx.datetime.Clock.System.now()
                // Parse ISO datetime (handle various formats)
                val cleaned = dateTimeStr
                    .replace(" ", "T")
                    .let { if (it.contains("+") || it.endsWith("Z")) it else "${it}Z" }
                val eventInstant = kotlinx.datetime.Instant.parse(cleaned)

                val diffMs = (now - eventInstant).inWholeMilliseconds
                // Event is active if it started (diffMs > 0) and within 3 hours (diffMs < 3h)
                diffMs in 0..10_800_000L
            } catch (e: Exception) {
                false
            }
        }
    }
}
