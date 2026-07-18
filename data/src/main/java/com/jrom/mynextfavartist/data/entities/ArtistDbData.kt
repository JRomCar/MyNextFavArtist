package com.jrom.mynextfavartist.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistDbData(
    @PrimaryKey val mbid: String,
    val name: String,
    val type: String?,
    val country: String?,
    val disambiguation: String?,
)
