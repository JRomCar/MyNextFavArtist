package com.jrom.mynextfavartist.domain.entities

import androidx.compose.runtime.Immutable

@Immutable
data class ReleaseGroup(
    val mbid: String,
    val title: String,
    val primaryType: String?,
    val firstReleaseDate: String?,
)
