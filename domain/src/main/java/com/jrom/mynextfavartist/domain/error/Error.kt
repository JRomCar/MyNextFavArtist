package com.jrom.mynextfavartist.domain.error

/**
 * Marker bounding [com.jrom.mynextfavartist.domain.Result]'s error type parameter, so any use
 * case or repository can be generic over "some domain error" without committing to which one -
 * [DataError] is the only implementation today, but a future error family wouldn't need
 * changes to [com.jrom.mynextfavartist.domain.Result] itself.
 */
sealed interface Error
