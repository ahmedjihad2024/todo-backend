package org.personal.todo.common.storage

/** Whether a category's files are world-readable or require an authorization check. */
enum class Visibility { PUBLIC, PRIVATE }

private const val MB = 1024L * 1024L

/**
 * The single source of truth for every MIME type the app accepts, mapped to the
 * file extension stored on disk / in object storage. Add a row here and it flows
 * everywhere: the allow-lists below and the extension used when saving a file.
 */
val MEDIA_EXTENSIONS: Map<String, String> = mapOf(
    "image/png" to ".png",
    "image/jpeg" to ".jpg",
    "image/webp" to ".webp",
    "application/pdf" to ".pdf",
)

/** All accepted image types, derived from [MEDIA_EXTENSIONS] (everything under "image/"). */
private val IMAGE_TYPES = MEDIA_EXTENSIONS.keys.filter { it.startsWith("image/") }.toSet()

/**
 * A kind of uploaded file, carrying its own storage rules. This is what lets a
 * single [FileStorageService] handle everything — you pass the category and it
 * applies that category's prefix, size cap, allowed MIME types, and visibility.
 *
 *  - [prefix]       the storage-key folder, also the public URL segment ("avatars/" → /media/avatars/...)
 *  - [maxBytes]     upload size cap
 *  - [allowedTypes] MIME types accepted, enforced by magic-byte sniffing (not the filename)
 *  - [visibility]   PUBLIC files are servable via /media; PRIVATE files need an authorized endpoint
 */
enum class FileCategory(
    val prefix: String,
    val maxBytes: Long,
    val allowedTypes: Set<String>,
    val visibility: Visibility,
) {
    AVATAR("avatars/", 5 * MB, IMAGE_TYPES, Visibility.PUBLIC),
    IMAGES("images/", 5 * MB, IMAGE_TYPES, Visibility.PUBLIC),
    TAX_RECORD("tax-records/", 10 * MB, IMAGE_TYPES + "application/pdf", Visibility.PRIVATE),
    ;

    companion object {
        /** Resolves a category from a URL path segment (e.g. "avatars" → [AVATAR]), or null if unknown. */
        fun fromSegment(segment: String): FileCategory? =
            entries.firstOrNull { it.prefix == "$segment/" }
    }
}
