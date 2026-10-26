package org.personal.todo.common.storage

import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

/**
 * Serves a PRIVATE [StoredFile] as an HTTP response. Unlike the public /media path, private files use
 * `Cache-Control: no-store` so sensitive documents (e.g. tax records) are never cached by browsers or
 * shared proxies. Authorization is the caller's responsibility — only return this after the caller has
 * been verified as an owner or admin.
 */
fun StoredFile.toPrivateResponseEntity(): ResponseEntity<Resource> =
    ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .cacheControl(CacheControl.noStore())
        .body(resource)
