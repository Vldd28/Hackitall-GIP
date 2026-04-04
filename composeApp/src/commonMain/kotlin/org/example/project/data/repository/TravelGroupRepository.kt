package org.example.project.data.repository

import io.github.jan.supabase.postgrest.from
import org.example.project.data.model.Event
import org.example.project.data.model.TravelGroup
import org.example.project.data.model.TravelGroupInsert
import org.example.project.data.model.TravelGroupMember
import org.example.project.data.remote.supabase

class TravelGroupRepository {

    suspend fun getOpenGroups(): List<TravelGroup> =
        supabase.from("travel_groups").select {
            filter { eq("is_open", true) }
        }.decodeList()

    suspend fun getGroupById(groupId: String): TravelGroup =
        supabase.from("travel_groups").select {
            filter { eq("id", groupId) }
        }.decodeSingle()

    suspend fun createGroup(insert: TravelGroupInsert): TravelGroup {
        val group = supabase.from("travel_groups").insert(insert) {
            select()
        }.decodeSingle<TravelGroup>()
        // Auto-join creator as admin
        supabase.from("travel_group_members").insert(
            TravelGroupMember(groupId = group.id, profileId = insert.creatorId, role = "admin")
        )
        return group
    }

    suspend fun joinGroup(groupId: String, userId: String) {
        supabase.from("travel_group_members").insert(
            TravelGroupMember(groupId = groupId, profileId = userId)
        )
    }

    suspend fun leaveGroup(groupId: String, userId: String) {
        supabase.from("travel_group_members").delete {
            filter {
                eq("group_id", groupId)
                eq("profile_id", userId)
            }
        }
    }

    suspend fun getGroupMembers(groupId: String): List<TravelGroupMember> =
        supabase.from("travel_group_members").select {
            filter { eq("group_id", groupId) }
        }.decodeList()

    suspend fun getGroupEvents(groupId: String): List<Event> =
        supabase.from("events").select {
            filter { eq("group_id", groupId) }
        }.decodeList()

    suspend fun getUserGroups(userId: String): List<TravelGroup> {
        val groupIds = supabase.from("travel_group_members").select {
            filter { eq("profile_id", userId) }
        }.decodeList<TravelGroupMember>().map { it.groupId }.toSet()

        if (groupIds.isEmpty()) return emptyList()
        // Fetch all groups and filter in memory — avoids SQL IN complexity
        return supabase.from("travel_groups").select()
            .decodeList<TravelGroup>()
            .filter { it.id in groupIds }
    }
}
