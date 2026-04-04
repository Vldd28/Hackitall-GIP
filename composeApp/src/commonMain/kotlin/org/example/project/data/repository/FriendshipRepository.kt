package org.example.project.data.repository

import io.github.jan.supabase.postgrest.from
import org.example.project.data.model.Friendship
import org.example.project.data.model.FriendshipStatus
import org.example.project.data.model.FriendshipStatusUpdate
import org.example.project.data.remote.supabase

class FriendshipRepository {

    suspend fun sendRequest(requesterId: String, receiverId: String) {
        supabase.from("friendships").insert(
            Friendship(requesterId = requesterId, receiverId = receiverId)
        )
    }

    suspend fun acceptRequest(requesterId: String, receiverId: String) {
        supabase.from("friendships").update(
            FriendshipStatusUpdate(status = FriendshipStatus.accepted)
        ) {
            filter {
                eq("requester_id", requesterId)
                eq("receiver_id", receiverId)
            }
        }
    }

    suspend fun blockUser(requesterId: String, receiverId: String) {
        supabase.from("friendships").update(
            FriendshipStatusUpdate(status = FriendshipStatus.blocked)
        ) {
            filter {
                eq("requester_id", requesterId)
                eq("receiver_id", receiverId)
            }
        }
    }

    suspend fun removeFriendship(userA: String, userB: String) {
        // Delete whichever direction the friendship was created in
        supabase.from("friendships").delete {
            filter {
                eq("requester_id", userA)
                eq("receiver_id", userB)
            }
        }
        supabase.from("friendships").delete {
            filter {
                eq("requester_id", userB)
                eq("receiver_id", userA)
            }
        }
    }

    suspend fun getFriends(userId: String): List<Friendship> {
        val asSender = supabase.from("friendships").select {
            filter {
                eq("requester_id", userId)
                eq("status", "accepted")
            }
        }.decodeList<Friendship>()

        val asReceiver = supabase.from("friendships").select {
            filter {
                eq("receiver_id", userId)
                eq("status", "accepted")
            }
        }.decodeList<Friendship>()

        return asSender + asReceiver
    }

    suspend fun getPendingRequests(userId: String): List<Friendship> =
        supabase.from("friendships").select {
            filter {
                eq("receiver_id", userId)
                eq("status", "pending")
            }
        }.decodeList()

    suspend fun getFriendshipStatus(userId: String, otherId: String): FriendshipStatus? {
        val direct = supabase.from("friendships").select {
            filter {
                eq("requester_id", userId)
                eq("receiver_id", otherId)
            }
        }.decodeSingleOrNull<Friendship>()
        if (direct != null) return direct.status

        val inverse = supabase.from("friendships").select {
            filter {
                eq("requester_id", otherId)
                eq("receiver_id", userId)
            }
        }.decodeSingleOrNull<Friendship>()
        return inverse?.status
    }

    // Helper: given a friendship, return the ID of the other person.
    fun getFriendId(currentUserId: String, friendship: Friendship): String =
        if (friendship.requesterId == currentUserId) friendship.receiverId
        else friendship.requesterId
}
