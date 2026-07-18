package com.jrom.mynextfavartist.data.api

import com.jrom.mynextfavartist.data.entities.ArtistSearchResponse
import com.jrom.mynextfavartist.data.entities.ReleaseGroupBrowseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicBrainzApi {
    @GET("artist")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("fmt") fmt: String = "json",
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
    ): ArtistSearchResponse

    @GET("release-group")
    suspend fun getReleaseGroupsForArtist(
        @Query("artist") artistMbid: String,
        @Query("type") type: String = "album",
        @Query("fmt") fmt: String = "json",
        @Query("limit") limit: Int = 100,
    ): ReleaseGroupBrowseResponse
}
