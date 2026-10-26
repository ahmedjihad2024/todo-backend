package org.personal.todo.common.extentions

import org.personal.todo.sharelink.ShareEntity

import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

/**
 * Builds the absolute public share link for an entity (e.g. "https://host/bookings/<id>"),
 * deriving scheme/host from the current request so the same id resolves in every environment.
 * The returned URL is what the apps and chat previews open. Must be called on an HTTP request thread.
 */
fun UUID.toShareLink(type: ShareEntity): String =
    ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/${type.segment}/$this")
        .toUriString()
