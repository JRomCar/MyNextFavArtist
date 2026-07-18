package com.jrom.mynextfavartist.data.entities

import com.google.gson.annotations.SerializedName

// Response for the artist search endpoint (/artist)
data class ArtistSearchResponse(
    @SerializedName("count") val count: Int? = null,
    @SerializedName("offset") val offset: Int? = null,
    @SerializedName("artists") val artists: List<ArtistData>? = emptyList(),
)

data class ArtistData(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String? = null,
    @SerializedName("disambiguation") val disambiguation: String? = null,
    @SerializedName("score") val score: Int? = null,
)
