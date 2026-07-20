package com.jrom.mynextfavartist.ui.navigation

import com.jrom.mynextfavartist.domain.entities.Artist
import kotlinx.serialization.Serializable

// Artist's nav-route shape, kept separate from the domain entity so a UI routing decision
// (navigation3's NavKey requires @Serializable) doesn't dictate domain's dependencies.
@Serializable
data class ArtistNavArg(
    val mbid: String,
    val name: String,
    val type: String?,
    val country: String?,
    val disambiguation: String?,
)

fun Artist.toNavArg() = ArtistNavArg(
    mbid = mbid,
    name = name,
    type = type,
    country = country,
    disambiguation = disambiguation,
)

fun ArtistNavArg.toDomain() = Artist(
    mbid = mbid,
    name = name,
    type = type,
    country = country,
    disambiguation = disambiguation,
)
