package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.repository.ArtistRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetHomeArtistsTest {

    private val artistRepository: ArtistRepository = mock()
    private val sut = GetHomeArtists(artistRepository)

    @Test
    fun `builds a single arid OR query covering every seed MBID`() = runTest {
        whenever(artistRepository.searchArtists(any(), any(), any()))
            .thenReturn(Result.Success<List<Artist>, DataError.Network>(emptyList()))

        sut()

        val expectedQuery = SeedArtists.mbids.joinToString(separator = " OR ", prefix = "arid:(", postfix = ")")
        verify(artistRepository).searchArtists(
            query = eq(expectedQuery),
            limit = eq(SeedArtists.mbids.size),
            offset = eq(0),
        )
    }

    @Test
    fun `delegates the repository result unchanged`() = runTest {
        val artists = listOf(
            Artist(mbid = "1", name = "Radiohead", type = "Group", country = "GB", disambiguation = null)
        )
        whenever(artistRepository.searchArtists(any(), any(), any()))
            .thenReturn(Result.Success<List<Artist>, DataError.Network>(artists))

        val result = sut()

        assertEquals(Result.Success<List<Artist>, DataError.Network>(artists), result)
    }
}
