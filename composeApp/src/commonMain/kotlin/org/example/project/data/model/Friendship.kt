package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipStatus {
    @SerialName("pending") pending,
    @SerialName("accepted") accepted,
    @SerialName("blocked") blocked
}

@Serializable
data class FriendshipStatusUpdate(val status: FriendshipStatus)

@Serializable
data class Friendship(
    @SerialName("requester_id") val requesterId: String,
    @SerialName("receiver_id") val receiverId: String,
    val status: FriendshipStatus = FriendshipStatus.pending,
    @SerialName("created_at") val createdAt: String? = null
)
