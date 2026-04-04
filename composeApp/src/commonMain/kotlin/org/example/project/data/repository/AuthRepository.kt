package org.example.project.data.repository

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.StateFlow
import org.example.project.data.model.Profile
import org.example.project.data.remote.supabase

class AuthRepository {

    val sessionStatus: StateFlow<SessionStatus>
        get() = supabase.auth.sessionStatus

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    suspend fun signUp(email: String, password: String, username: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        // Upsert profile — trigger may have already created the row with just the id,
        // so we upsert to set the username without failing on conflict.
        supabase.from("profiles").upsert(
            Profile(id = userId, username = username)
        )
    }

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }
}
