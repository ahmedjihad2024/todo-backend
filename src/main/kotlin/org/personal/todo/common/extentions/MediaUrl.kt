package org.personal.todo.common.extentions

import org.personal.todo.common.storage.FileCategory
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

/**
 * Builds the absolute public URL for a stored file (e.g. "https://host/media/avatars/<file>"),
 * deriving scheme/host from the current request so the same stored filename works in every
 * environment. Returns null when the receiver (the stored filename) is null.
 *
 * Only use this for PUBLIC categories — PRIVATE files (e.g. tax records) must be served by an
 * authorized, feature-specific endpoint, never a guessable public URL. Must be called on an
 * HTTP request thread.
 */
fun String?.toMediaUrl(category: FileCategory): String? =
    this?.let {
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/media/${category.prefix}$it")
            .toUriString()
    }
