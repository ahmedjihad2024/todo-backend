package org.personal.todo.common.storage

import org.apache.tika.Tika
import org.personal.todo.common.exception.ApiException
import org.personal.todo.common.storage.backend.FileStorage
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/** A stored file resolved from the backend, paired with the MIME type to serve it as. */
data class StoredFile(val resource: Resource, val contentType: String)

/**
 * The single entry point for storing every kind of uploaded file. The [FileCategory]
 * argument decides the rules — folder, size cap, allowed types, visibility — so there's
 * no per-feature storage class (no AvatarStorage / ProjectStorage / TaxStorage).
 *
 * Validation is by real magic bytes, never the client filename, and the stored name is
 * always a server-generated UUID. The bare "<uuid>.<ext>" filename is what callers persist
 * (e.g. on the user/project/store document); the category supplies the rest of the key.
 *
 * Storing is unified here; *serving* is not — PUBLIC files go through the open /media
 * endpoint, while PRIVATE files (e.g. tax records) must be served by a feature-specific
 * endpoint that authorizes the caller first.
 */
@Service
class FileStorageService(
    private val storage: FileStorage,
) {
    // Tika is thread-safe; one shared instance detects the MIME type from content.
    private val tika = Tika()

    // Reverse of MEDIA_EXTENSIONS (the single source of truth in FileCategory),
    // used to pick the content type when serving a stored file back.
    private val typeByExtension = MEDIA_EXTENSIONS.entries.associate { (type, ext) -> ext to type }

    /** Validates [file] against [category] and stores it, returning the generated "<uuid>.<ext>" filename. */
    fun store(category: FileCategory, file: MultipartFile): String {
        if (file.isEmpty) throw ApiException.BadRequest("error.file.empty")
        return store(category, file.bytes)
    }

    /**
     * Stores raw [bytes] under [category] with the same validation and UUID naming as the
     * [MultipartFile] overload. Use this when the bytes don't come from an HTTP upload —
     * e.g. seeding reference data or a server-side fetch.
     */
    fun store(category: FileCategory, bytes: ByteArray): String {
        if (bytes.isEmpty()) throw ApiException.BadRequest("error.file.empty")
        if (bytes.size > category.maxBytes) throw ApiException.BadRequest("error.file.too_large")

        // Detect the true MIME type from the bytes (not any filename), then
        // enforce it against the category's allow-list.
        val detectedType = tika.detect(bytes)
        if (detectedType !in category.allowedTypes) {
            throw ApiException.BadRequest("error.file.invalid_type")
        }

        val filename = "${UUID.randomUUID()}${MEDIA_EXTENSIONS.getValue(detectedType)}"
        storage.put(category.prefix + filename, bytes, detectedType)
        return filename
    }

    /** Creates a copy of an existing stored file under a new UUID-based name, or null if the source doesn't exist. */
    fun copy(category: FileCategory, filename: String): String? {
        val key = keyFor(category, filename) ?: return null
        val bytes = storage.get(key) ?: return null
        val ext = extensionOf(filename)
        val contentType = typeByExtension[ext] ?: "application/octet-stream"
        val newFilename = "${UUID.randomUUID()}$ext"
        storage.put(category.prefix + newFilename, bytes, contentType)
        return newFilename
    }

    /** Loads [filename] within [category] as a servable resource, or null if missing or the name is unsafe. */
    @Suppress("Xss")
    fun load(category: FileCategory, filename: String): StoredFile? {
        val key = keyFor(category, filename) ?: return null
        val bytes = storage.get(key) ?: return null
        val contentType = typeByExtension[extensionOf(filename)] ?: "application/octet-stream"
        return StoredFile(ByteArrayResource(bytes), contentType)
    }

    /** Deletes [filename] within [category]; a no-op if it doesn't exist or the name is unsafe. */
    fun delete(category: FileCategory, filename: String) {
        val key = keyFor(category, filename) ?: return
        storage.delete(key)
    }

    fun compress(
        resource: Resource,
        quality: Int,
        scale: Int
    ): Resource = storage.compress(resource, quality, scale)

    /**
     * Builds the storage key for a stored [filename] in [category], rejecting anything that
     * isn't a bare filename (no path separators or traversal) as a defense-in-depth check.
     */
    private fun keyFor(category: FileCategory, filename: String): String? {
        if (filename.isBlank() || filename.contains('/') || filename.contains('\\') || filename.contains("..")) {
            return null
        }
        return category.prefix + filename
    }

    private fun extensionOf(filename: String): String =
        filename.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it".lowercase() }
}
