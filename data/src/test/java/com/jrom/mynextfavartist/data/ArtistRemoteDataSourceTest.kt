package com.jrom.mynextfavartist.data

import com.jrom.mynextfavartist.data.MockData.testArtistSearchResponse
import com.jrom.mynextfavartist.data.MockData.testArtistsEntityList
import com.jrom.mynextfavartist.data.MockData.testReleaseGroupBrowseResponse
import com.jrom.mynextfavartist.data.MockData.testReleaseGroupsEntityList
import com.jrom.mynextfavartist.data.api.MusicBrainzApi
import com.jrom.mynextfavartist.data.entities.ReleaseGroupBrowseResponse
import com.jrom.mynextfavartist.data.entities.ReleaseGroupData
import com.jrom.mynextfavartist.data.repository.ArtistRemoteDataSource
import com.jrom.mynextfavartist.domain.dataOrNull
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.errorOrNull
import com.jrom.mynextfavartist.testutils.TestBase
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ArtistRemoteDataSourceTest : TestBase() {

    private val musicBrainzApi: MusicBrainzApi = mock()

    private lateinit var sut: ArtistRemoteDataSource

    @Before
    fun setUp() {
        sut = ArtistRemoteDataSource(unconfinedTestDispatcher, musicBrainzApi)
    }

    @Test
    fun `searchArtists returns success when API call is successful`() = runUnconfinedTest {
        whenever(musicBrainzApi.searchArtists(any(), any(), any(), any()))
            .thenReturn(testArtistSearchResponse)

        val result = sut.searchArtists("Radiohead", 30, 0)

        assertTrue(result.isSuccess)
        assertEquals(testArtistsEntityList, result.dataOrNull)
    }

    @Test
    fun `searchArtists returns error when API responds 400`() = runUnconfinedTest {
        whenever(musicBrainzApi.searchArtists(any(), any(), any(), any()))
            .thenThrow(createHttpException(400))

        val result = sut.searchArtists("query", 30, 0)

        assertTrue(result.isFailure)
        assertEquals(DataError.Network.BAD_REQUEST, result.errorOrNull)
    }

    @Test
    fun `searchArtists returns error when API responds 404`() = runUnconfinedTest {
        whenever(musicBrainzApi.searchArtists(any(), any(), any(), any()))
            .thenThrow(createHttpException(404))

        val result = sut.searchArtists("query", 30, 0)

        assertTrue(result.isFailure)
        assertEquals(DataError.Network.NOT_FOUND, result.errorOrNull)
    }

    @Test
    fun `searchArtists returns rate limited error when API responds 503`() = runUnconfinedTest {
        whenever(musicBrainzApi.searchArtists(any(), any(), any(), any()))
            .thenThrow(createHttpException(503))

        val result = sut.searchArtists("query", 30, 0)

        assertTrue(result.isFailure)
        assertEquals(DataError.Network.RATE_LIMITED, result.errorOrNull)
    }

    @Test
    fun `searchArtists returns timeout error on SocketTimeoutException`() = runUnconfinedTest {
        whenever(musicBrainzApi.searchArtists(any(), any(), any(), any()))
            .thenAnswer { throw SocketTimeoutException("timed out") }

        val result = sut.searchArtists("query", 30, 0)

        assertTrue(result.isFailure)
        assertEquals(DataError.Network.TIMEOUT, result.errorOrNull)
    }

    @Test
    fun `searchArtists returns unknown error when connectivity fails with IOException`() = runUnconfinedTest {
        whenever(musicBrainzApi.searchArtists(any(), any(), any(), any()))
            .thenAnswer { throw IOException("No network") }

        val result = sut.searchArtists("query", 30, 0)

        assertTrue(result.isFailure)
        assertEquals(DataError.Network.UNKNOWN, result.errorOrNull)
    }

    @Test
    fun `getArtistReleaseGroups returns success sorted by first release date`() = runUnconfinedTest {
        whenever(musicBrainzApi.getReleaseGroupsForArtist(any(), anyOrNull(), any(), any(), any()))
            .thenReturn(testReleaseGroupBrowseResponse)

        val result = sut.getArtistReleaseGroups("mbid")

        assertTrue(result.isSuccess)
        assertEquals(testReleaseGroupsEntityList, result.dataOrNull)
    }

    @Test
    fun `getArtistReleaseGroups paginates until all release groups are fetched`() = runUnconfinedTest {
        val page1 = ReleaseGroupBrowseResponse(
            releaseGroupCount = 2,
            releaseGroupOffset = 0,
            releaseGroups = listOf(
                ReleaseGroupData(
                    id = "b1392450-e666-3926-a536-22c65f834433",
                    title = "OK Computer",
                    primaryType = "Album",
                    firstReleaseDate = "1997-05-21",
                )
            ),
        )
        val page2 = ReleaseGroupBrowseResponse(
            releaseGroupCount = 2,
            releaseGroupOffset = 1,
            releaseGroups = listOf(
                ReleaseGroupData(
                    id = "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386",
                    title = "In Rainbows",
                    primaryType = "Album",
                    firstReleaseDate = "2007-10-10",
                )
            ),
        )
        whenever(musicBrainzApi.getReleaseGroupsForArtist(any(), anyOrNull(), any(), any(), eq(0)))
            .thenReturn(page1)
        whenever(musicBrainzApi.getReleaseGroupsForArtist(any(), anyOrNull(), any(), any(), eq(1)))
            .thenReturn(page2)

        val result = sut.getArtistReleaseGroups("mbid")

        assertTrue(result.isSuccess)
        assertEquals(2, result.dataOrNull?.size)
        verify(musicBrainzApi, times(2)).getReleaseGroupsForArtist(any(), anyOrNull(), any(), any(), any())
    }

    @Test
    fun `getArtistReleaseGroups sorts newest first with undated releases last`() = runUnconfinedTest {
        val undated = ReleaseGroupData(
            id = "3b5e4c1a-1111-4a6d-8f2a-000000000001",
            title = "Untitled Live Bootleg",
            primaryType = "Live",
            firstReleaseDate = null,
        )
        val okComputer = ReleaseGroupData(
            id = "b1392450-e666-3926-a536-22c65f834433",
            title = "OK Computer",
            primaryType = "Album",
            firstReleaseDate = "1997-05-21",
        )
        val inRainbows = ReleaseGroupData(
            id = "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386",
            title = "In Rainbows",
            primaryType = "Album",
            firstReleaseDate = "2007-10-10",
        )
        val response = ReleaseGroupBrowseResponse(
            releaseGroupCount = 3,
            releaseGroupOffset = 0,
            releaseGroups = listOf(okComputer, undated, inRainbows),
        )
        whenever(musicBrainzApi.getReleaseGroupsForArtist(any(), anyOrNull(), any(), any(), any()))
            .thenReturn(response)

        val result = sut.getArtistReleaseGroups("mbid")

        assertEquals(
            listOf("In Rainbows", "OK Computer", "Untitled Live Bootleg"),
            result.dataOrNull?.map { it.title },
        )
    }

    @Test
    fun `getArtistReleaseGroups returns error when API responds 500`() = runUnconfinedTest {
        whenever(musicBrainzApi.getReleaseGroupsForArtist(any(), anyOrNull(), any(), any(), any()))
            .thenThrow(createHttpException(500))

        val result = sut.getArtistReleaseGroups("mbid")

        assertTrue(result.isFailure)
        assertEquals(DataError.Network.SERVER_ERROR, result.errorOrNull)
    }

    private fun createHttpException(statusCode: Int): HttpException {
        val emptyErrorBody = "".toResponseBody(null)
        val errorResponse: Response<*> = Response.error<Any>(statusCode, emptyErrorBody)
        return HttpException(errorResponse)
    }
}
