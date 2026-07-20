package com.jrom.mynextfavartist.data.entities

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

// No existing test exercises the real JSON converter - ArtistRemoteDataSourceTest mocks
// MusicBrainzApi directly, so a wrong @SerialName (hyphenated MusicBrainz field names are easy
// to typo) would compile fine and pass every other test while silently deserializing to null in
// production.
class SerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes an artist search response with hyphen-free field names`() {
        val response = json.decodeFromString<ArtistSearchResponse>(
            """
            {
              "count": 1,
              "offset": 0,
              "artists": [
                {
                  "id": "a74b1b7f-71a5-4011-9441-d0b5e4122711",
                  "type": "Group",
                  "name": "Radiohead",
                  "country": "GB",
                  "disambiguation": "",
                  "score": 100
                }
              ]
            }
            """.trimIndent(),
        )

        assertEquals(1, response.count)
        assertEquals(0, response.offset)
        assertEquals(
            ArtistData(
                id = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
                type = "Group",
                name = "Radiohead",
                country = "GB",
                disambiguation = "",
                score = 100,
            ),
            response.artists?.single(),
        )
    }

    @Test
    fun `decodes a release-group response with hyphenated field names`() {
        val response = json.decodeFromString<ReleaseGroupBrowseResponse>(
            """
            {
              "release-group-count": 1,
              "release-group-offset": 0,
              "release-groups": [
                {
                  "id": "b1392450-e666-3926-a536-22c65f834433",
                  "title": "OK Computer",
                  "primary-type": "Album",
                  "first-release-date": "1997-05-21"
                }
              ]
            }
            """.trimIndent(),
        )

        assertEquals(1, response.releaseGroupCount)
        assertEquals(0, response.releaseGroupOffset)
        assertEquals(
            ReleaseGroupData(
                id = "b1392450-e666-3926-a536-22c65f834433",
                title = "OK Computer",
                primaryType = "Album",
                firstReleaseDate = "1997-05-21",
            ),
            response.releaseGroups?.single(),
        )
    }

    @Test
    fun `tolerates unmapped fields and missing optional fields`() {
        val response = json.decodeFromString<ArtistSearchResponse>(
            """{"artists": [{"id": "mbid", "name": "Artist", "unmapped-field": "ignored"}]}""",
        )

        assertEquals(
            ArtistData(id = "mbid", name = "Artist"),
            response.artists?.single(),
        )
    }
}
