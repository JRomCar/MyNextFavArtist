package com.jrom.mynextfavartist.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Response for the release-group browse endpoint (/release-group?artist=<mbid>)
@Serializable
data class ReleaseGroupBrowseResponse(
    @SerialName("release-group-count") val releaseGroupCount: Int? = null,
    @SerialName("release-group-offset") val releaseGroupOffset: Int? = null,
    @SerialName("release-groups") val releaseGroups: List<ReleaseGroupData>? = emptyList(),
)

// id/title are nullable for the same reason as ArtistData.id/name - see the comment there.
@Serializable
data class ReleaseGroupData(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("primary-type") val primaryType: String? = null,
    @SerialName("first-release-date") val firstReleaseDate: String? = null,
)
