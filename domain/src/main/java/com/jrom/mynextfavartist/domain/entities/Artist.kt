package com.jrom.mynextfavartist.domain.entities

import androidx.compose.runtime.Immutable

@Immutable
data class Artist(
    val mbid: String,
    val name: String,
    val type: String?,
    val country: String?,
    val disambiguation: String?,
)
