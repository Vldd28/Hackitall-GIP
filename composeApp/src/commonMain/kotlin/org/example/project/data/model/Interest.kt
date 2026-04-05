package org.example.project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Interest(
    val id: Int,
    val name: String,
    val icon: String? = null
)
