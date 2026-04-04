package org.example.project.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import org.example.project.data.model.Interest
import org.example.project.data.model.Profile
import org.example.project.data.model.ProfileInterest
import org.example.project.data.model.ProfileUpdate
import org.example.project.data.remote.supabase

class UserRepository {

    suspend fun getProfile(userId: String): Profile =
        supabase.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeSingle()

    suspend fun updateProfile(userId: String, update: ProfileUpdate): Profile {
        supabase.from("profiles").update(update) {
            filter { eq("id", userId) }
        }
        return getProfile(userId)
    }

    suspend fun uploadAvatar(userId: String, data: ByteArray): String {
        supabase.storage.from("avatars").upload("$userId/avatar.jpg", data) {
            upsert = true
        }
        return supabase.storage.from("avatars").publicUrl("$userId/avatar.jpg")
    }

    suspend fun getAllInterests(): List<Interest> =
        supabase.from("interests").select().decodeList()

    suspend fun getUserInterests(userId: String): List<Interest> {
        val ids = supabase.from("profile_interests").select {
            filter { eq("profile_id", userId) }
        }.decodeList<ProfileInterest>().map { it.interestId }.toSet()

        if (ids.isEmpty()) return emptyList()
        return getAllInterests().filter { it.id in ids }
    }

    suspend fun setUserInterests(userId: String, interestIds: List<Int>) {
        supabase.from("profile_interests").delete {
            filter { eq("profile_id", userId) }
        }
        if (interestIds.isNotEmpty()) {
            supabase.from("profile_interests").insert(
                interestIds.map { ProfileInterest(profileId = userId, interestId = it) }
            )
        }
    }
}
