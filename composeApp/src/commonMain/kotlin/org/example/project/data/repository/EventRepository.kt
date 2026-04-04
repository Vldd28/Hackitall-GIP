package org.example.project.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import org.example.project.data.model.Event
import org.example.project.data.model.EventInsert
import org.example.project.data.model.EventParticipant
import org.example.project.data.remote.supabase

class EventRepository {

    suspend fun getPublicEvents(): List<Event> =
        supabase.from("events").select {
            filter { eq("is_public", true) }
            order("date_time", Order.ASCENDING)
        }.decodeList()

    suspend fun getEventById(eventId: String): Event =
        supabase.from("events").select {
            filter { eq("id", eventId) }
        }.decodeSingle()

    suspend fun createEvent(insert: EventInsert): Event {
        val event = supabase.from("events").insert(insert) {
            select()
        }.decodeSingle<Event>()
        // Auto-join creator as a participant
        supabase.from("event_participants").insert(
            EventParticipant(eventId = event.id, profileId = insert.creatorId)
        )
        return event
    }

    suspend fun joinEvent(eventId: String, userId: String) {
        supabase.from("event_participants").insert(
            EventParticipant(eventId = eventId, profileId = userId)
        )
    }

    suspend fun leaveEvent(eventId: String, userId: String) {
        supabase.from("event_participants").delete {
            filter {
                eq("event_id", eventId)
                eq("profile_id", userId)
            }
        }
    }

    suspend fun getEventParticipants(eventId: String): List<EventParticipant> =
        supabase.from("event_participants").select {
            filter { eq("event_id", eventId) }
        }.decodeList()

    suspend fun getUserCreatedEvents(userId: String): List<Event> =
        supabase.from("events").select {
            filter { eq("creator_id", userId) }
            order("date_time", Order.ASCENDING)
        }.decodeList()

    suspend fun getUserJoinedEvents(userId: String): List<Event> {
        val joinedIds = supabase.from("event_participants").select {
            filter { eq("profile_id", userId) }
        }.decodeList<EventParticipant>().map { it.eventId }.toSet()

        if (joinedIds.isEmpty()) return emptyList()
        return getPublicEvents().filter { it.id in joinedIds }
    }
}
