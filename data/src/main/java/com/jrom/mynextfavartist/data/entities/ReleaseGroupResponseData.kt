package com.jrom.mynextfavartist.data.entities

import com.google.gson.annotations.SerializedName

// Response for the release-group browse endpoint (/release-group?artist=<mbid>)
data class ReleaseGroupBrowseResponse(
    @SerializedName("release-group-count") val releaseGroupCount: Int? = null,
    @SerializedName("release-group-offset") val releaseGroupOffset: Int? = null,
    @SerializedName("release-groups") val releaseGroups: List<ReleaseGroupData>? = emptyList(),
)

// id/title are nullable for the same reason as ArtistData.id/name - see the comment there.
data class ReleaseGroupData(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("primary-type") val primaryType: String? = null,
    @SerializedName("first-release-date") val firstReleaseDate: String? = null,
)
