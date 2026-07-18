package com.jrom.mynextfavartist.ui.error

import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.error.UiText.StringResource

fun DataError.asUiText(): UiText {
    return when (this) {
        DataError.Network.BAD_REQUEST -> StringResource(R.string.bad_request)
        DataError.Network.NOT_FOUND -> StringResource(R.string.not_found)
        DataError.Network.RATE_LIMITED -> StringResource(R.string.rate_limited)
        DataError.Network.TIMEOUT -> StringResource(R.string.request_timeout)
        DataError.Network.SERVER_ERROR -> StringResource(R.string.server_error)
        DataError.Network.UNKNOWN -> StringResource(R.string.unknown_error)
        DataError.Local.DB_READ_ERROR -> StringResource(R.string.db_read_error)
        DataError.Local.DB_WRITE_ERROR -> StringResource(R.string.db_write_error)
    }
}
