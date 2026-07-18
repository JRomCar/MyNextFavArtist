package com.jrom.mynextfavartist.data

import com.jrom.mynextfavartist.data.entities.ArtistData
import com.jrom.mynextfavartist.data.entities.ArtistDbData
import com.jrom.mynextfavartist.data.entities.ArtistSearchResponse
import com.jrom.mynextfavartist.data.entities.ReleaseGroupBrowseResponse
import com.jrom.mynextfavartist.data.entities.ReleaseGroupData
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup

object MockData {

    val radioheadData = ArtistData(
        id = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
        type = "Group",
        name = "Radiohead",
        country = "GB",
        disambiguation = null,
        score = 100,
    )

    val radioheadEntity = Artist(
        mbid = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
        name = "Radiohead",
        type = "Group",
        country = "GB",
        disambiguation = null,
    )

    val testArtistsEntityList = listOf(radioheadEntity)

    val testArtistSearchResponse = ArtistSearchResponse(
        count = 1,
        offset = 0,
        artists = listOf(radioheadData),
    )

    val radioheadDbData = ArtistDbData(
        mbid = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
        name = "Radiohead",
        type = "Group",
        country = "GB",
        disambiguation = null,
    )

    val okComputerData = ReleaseGroupData(
        id = "b1392450-e666-3926-a536-22c65f834433",
        title = "OK Computer",
        primaryType = "Album",
        firstReleaseDate = "1997-05-21",
    )

    val okComputerEntity = ReleaseGroup(
        mbid = "b1392450-e666-3926-a536-22c65f834433",
        title = "OK Computer",
        primaryType = "Album",
        firstReleaseDate = "1997-05-21",
    )

    val testReleaseGroupBrowseResponse = ReleaseGroupBrowseResponse(
        releaseGroupCount = 1,
        releaseGroupOffset = 0,
        releaseGroups = listOf(okComputerData),
    )

    val testReleaseGroupsEntityList = listOf(okComputerEntity)
}
