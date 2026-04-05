package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileUpdate(
    val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val country: String? = null
)
