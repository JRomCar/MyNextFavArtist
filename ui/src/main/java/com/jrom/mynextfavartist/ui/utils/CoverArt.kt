package com.jrom.mynextfavartist.ui.utils

/**
 * Cover Art Archive's front-thumbnail URL is deterministic from a release-group's MBID, so
 * it's computed here rather than stored on the [com.jrom.mynextfavartist.domain.entities.ReleaseGroup]
 * entity, deriving a drawable resource from a climate string
 * instead of persisting one.
 */
fun releaseGroupCoverArtUrl(releaseGroupMbid: String): String =
    "https://coverartarchive.org/release-group/$releaseGroupMbid/front-250"
