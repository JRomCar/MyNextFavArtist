package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.repository.ArtistRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetHomeArtistsTest {

    private val artistRepository: ArtistRepository = mock()
    private val sut = GetHomeArtists(artistRepository)

    @Test
    fun `delegates to the repository's cached getHomeArtists`() = runTest {
        val artists = listOf(
            Artist(mbid = "1", name = "Radiohead", type = "Group", country = "GB", disambiguation = null)
        )
        whenever(artistRepository.getHomeArtists())
            .thenReturn(Result.Success<List<Artist>, DataError.Network>(artists))

        val result = sut()

        assertEquals(Result.Success<List<Artist>, DataError.Network>(artists), result)
    }
}
