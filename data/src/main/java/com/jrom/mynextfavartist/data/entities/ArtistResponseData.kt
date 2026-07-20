package com.jrom.mynextfavartist.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Response for the artist search endpoint (/artist)
@Serializable
data class ArtistSearchResponse(
    @SerialName("count") val count: Int? = null,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("artists") val artists: List<ArtistData>? = emptyList(),
)

// id/name are nullable despite MusicBrainz always documenting them as present: a single
// malformed artist entry should be dropped from the list (see toDomain() below), not fail the
// whole search response.
@Serializable
data class ArtistData(
    @SerialName("id") val id: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("disambiguation") val disambiguation: String? = null,
    @SerialName("score") val score: Int? = null,
)
