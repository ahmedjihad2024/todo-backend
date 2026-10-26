package org.personal.todo.common.extentions

import org.personal.todo.common.exception.ApiException
import java.util.UUID


/**
 * Parses a string into a [UUID], turning a malformed value into a clean 400
 * (`error.invalid_id`) instead of an unhandled IllegalArgumentException / 500.
 */
fun String.toUuid(): UUID =
    try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        throw ApiException.BadRequest("error.invalid_id")
    }

/**
 * Derives a globally-unique stock-keeping code from this id
 * (e.g. `SKU-65F0A1B2C3D44E5FA7B8C9D0E1F2A3B4`). A [UUID] is itself globally unique,
 * so the SKU is too: no counter table, no collision retry.
 */
fun UUID.toSku(): String = "SKU-${toString().replace("-", "").uppercase()}"
