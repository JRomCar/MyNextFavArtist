package com.jrom.mynextfavartist.domain.entities

data class Artist(
    val mbid: String,
    val name: String,
    val type: String?,
    val country: String?,
    val disambiguation: String?,
)
