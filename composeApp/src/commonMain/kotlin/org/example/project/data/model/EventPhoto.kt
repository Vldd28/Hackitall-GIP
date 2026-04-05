package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventPhoto(
    val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("uploaded_by") val uploadedBy: String,
    @SerialName("storage_path") val storagePath: String,
    val caption: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class EventPhotoInsert(
    @SerialName("event_id") val eventId: String,
    @SerialName("uploaded_by") val uploadedBy: String,
    @SerialName("storage_path") val storagePath: String,
    val caption: String? = null
)

/** Represents a past event with its photos, mapped from the profile_past_events view. */
@Serializable
data class PastEventWithPhotos(
    @SerialName("profile_id") val profileId: String,
    val id: String,
    val title: String,
    @SerialName("location_name") val locationName: String,
    @SerialName("date_time") val dateTime: String,
    @SerialName("picture_url") val pictureUrl: String? = null,
    @SerialName("location_image") val locationImage: String? = null,
    val photos: String? = null // JSON array string from view — parsed separately
)
