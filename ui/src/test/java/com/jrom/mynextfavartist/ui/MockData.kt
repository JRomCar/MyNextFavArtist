package com.jrom.mynextfavartist.ui

import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup

object MockData {

    val radioheadEntity = Artist(
        mbid = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
        name = "Radiohead",
        type = "Group",
        country = "GB",
        disambiguation = null,
    )

    val nirvanaEntity = Artist(
        mbid = "5b11f4ce-a62d-471e-81fc-a69a8278c7da",
        name = "Nirvana",
        type = "Group",
        country = "US",
        disambiguation = "1980s-1990s US grunge band",
    )

    val testArtistsEntityList = listOf(radioheadEntity, nirvanaEntity)

    val okComputerEntity = ReleaseGroup(
        mbid = "b1392450-e666-3926-a536-22c65f834433",
        title = "OK Computer",
        primaryType = "Album",
        firstReleaseDate = "1997-05-21",
    )

    val testReleaseGroupsEntityList = listOf(okComputerEntity)
}
