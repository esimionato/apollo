package io.muun.apollo.domain.errors.lnurl

import io.muun.apollo.domain.errors.MuunError

class NoRouteError(message: String, domain: String) : MuunError() {

    init {
        metadata["service"] = domain
        metadata["message"] = message
    }
}
