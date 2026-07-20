package com.jrom.mynextfavartist.data.entities

import com.google.gson.annotations.SerializedName

// Response for the artist search endpoint (/artist)
data class ArtistSearchResponse(
    @SerializedName("count") val count: Int? = null,
    @SerializedName("offset") val offset: Int? = null,
    @SerializedName("artists") val artists: List<ArtistData>? = emptyList(),
)

// id/name are nullable despite MusicBrainz always documenting them as present: Gson
// deserializes via reflection, bypassing Kotlin's constructor null-checks, so a response
// missing either field would otherwise produce a silent null in a "non-null" String and
// NPE somewhere downstream instead of failing where the bad data actually entered.
data class ArtistData(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String? = null,
    @SerializedName("name") val name: String?,
    @SerializedName("country") val country: String? = null,
    @SerializedName("disambiguation") val disambiguation: String? = null,
    @SerializedName("score") val score: Int? = null,
)
