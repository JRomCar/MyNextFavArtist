package com.jrom.mynextfavartist.data

import com.jrom.mynextfavartist.data.MockData.testArtistSearchResponse
import com.jrom.mynextfavartist.data.MockData.testArtistsEntityList
import com.jrom.mynextfavartist.data.MockData.testReleaseGroupBrowseResponse
import com.jrom.mynextfavartist.data.MockData.testReleaseGroupsEntityList
import com.jrom.mynextfavartist.data.api.MusicBrainzApi
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
import org.mockito.kotlin.mock
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
        whenever(musicBrainzApi.getReleaseGroupsForArtist(any(), any(), any(), any()))
            .thenReturn(testReleaseGroupBrowseResponse)

        val result = sut.getArtistReleaseGroups("mbid")

        assertTrue(result.isSuccess)
        assertEquals(testReleaseGroupsEntityList, result.dataOrNull)
    }

    @Test
    fun `getArtistReleaseGroups returns error when API responds 500`() = runUnconfinedTest {
        whenever(musicBrainzApi.getReleaseGroupsForArtist(any(), any(), any(), any()))
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
