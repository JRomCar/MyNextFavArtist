package com.jrom.mynextfavartist.domain.entities

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Artist(
    val mbid: String,
    val name: String,
    val type: String?,
    val country: String?,
    val disambiguation: String?,
)
