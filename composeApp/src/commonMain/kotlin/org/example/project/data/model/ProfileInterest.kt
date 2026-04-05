package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileInterest(
    @SerialName("profile_id") val profileId: String,
    @SerialName("interest_id") val interestId: Int
)
