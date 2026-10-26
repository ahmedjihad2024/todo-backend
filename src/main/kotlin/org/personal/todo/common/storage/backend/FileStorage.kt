package org.personal.todo.common.storage.backend
import net.coobird.thumbnailator.Thumbnails
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import java.io.ByteArrayOutputStream

/**
 * Abstraction over where uploaded files actually live. One implementation is
 * active at a time, chosen by the `app.storage.backend` property:
 *
 *  - `local` ([LocalFileStorage]) — files on the server's disk. Zero setup, used for dev.
 *  - `s3`    ([S3FileStorage])    — S3-compatible object storage (Cloudflare R2, MinIO,
 *                                   or AWS S3). Used in production: durable, replicated,
 *                                   and shared across all app instances.
 *
 * Keys are forward-slash paths (e.g. "avatars/<uuid>.png"). Callers own the key
 * scheme; this layer just stores, reads, and deletes bytes by key.
 */
interface FileStorage {

    /** Stores [bytes] under [key], overwriting any existing object. [contentType] is the MIME type to serve it as. */
    fun put(key: String, bytes: ByteArray, contentType: String)

    /** Returns the bytes stored under [key], or null if no such object exists. */
    fun get(key: String): ByteArray?

    /** Deletes the object at [key]; a no-op if it doesn't exist. */
    fun delete(key: String)

    fun compress(
        resource: Resource,
        quality: Int,
        scale: Int
    ): Resource {
        val output = ByteArrayOutputStream()

        Thumbnails.of(resource.inputStream)
            .scale(scale / 100.0) // Keeps the original dimensions
            .useOriginalFormat()
            .outputQuality(quality / 100.0)
            .toOutputStream(output)

        return ByteArrayResource(output.toByteArray())
    }
}
