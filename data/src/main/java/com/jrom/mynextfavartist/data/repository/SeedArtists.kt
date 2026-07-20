package com.jrom.mynextfavartist.data.repository

/**
 * MusicBrainz has no "trending"/"popular" or browse-all endpoint - only search-by-query
 * and lookup-by-MBID. This is a small curated list of well-known artist MBIDs (verified
 * against the live MusicBrainz search API, spanning rock/pop/hip-hop/jazz/electronic) used
 * to give the Home screen something to show without a real user query.
 */
object SeedArtists {
    val mbids: List<String> = listOf(
        "a74b1b7f-71a5-4011-9441-d0b5e4122711", // Radiohead
        "5b11f4ce-a62d-471e-81fc-a69a8278c7da", // Nirvana
        "0383dadf-2a4e-4d10-a46a-e9e041da8eb3", // Queen
        "b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d", // The Beatles
        "83d91898-7763-47d7-b03b-b92132375c47", // Pink Floyd
        "056e4f3e-d505-4dad-8ec1-d04f521cbb56", // Daft Punk
        "cc2c9c3c-b7bc-4b8b-84d8-4fbd8779e493", // Adele
        "20244d07-534f-4eff-b4d4-930878889970", // Taylor Swift
        "65f4f0c5-ef9e-490c-aee3-909e7ae6b2ab", // Metallica
        "5441c29d-3602-4898-b1a1-b77fa23b8e50", // David Bowie
        "bd13909f-1c29-4c27-a874-d4aaf27c5b1a", // Fleetwood Mac
        "678d88b2-87b0-403b-b63d-5da7465aecc3", // Led Zeppelin
        "b071f9fa-14b0-4217-8e97-eb41da73f598", // The Rolling Stones
        "f27ec8db-af05-4f36-916e-3d57f91ecf5e", // Michael Jackson
        "859d0860-d480-4efd-970c-c05d5f1776b8", // Beyoncé
        "381086ea-f511-4aba-bdf9-71c753dc5077", // Kendrick Lamar
        "561d854a-6a28-4aa7-8c99-323e6ce46c2a", // Miles Davis
        "72c536dc-7137-4477-a521-567eeb840fa8", // Bob Dylan
        "cc197bad-dc9c-440d-a5b5-d52ba2e14234", // Coldplay
        "87c5dedd-371d-4a53-9f7f-80522fb7f3cb", // Björk
    )
}
