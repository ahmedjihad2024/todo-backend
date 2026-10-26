package org.personal.todo.common.storage.backend

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Local-disk [FileStorage], active when `app.storage.backend` is `local` (the default).
 * Keys map to paths under [dir], so key "avatars/x.png" lands at "<dir>/avatars/x.png".
 *
 * Every resolved path is re-checked to stay within the base directory, so a crafted
 * key (e.g. "../../etc/passwd") can never escape it.
 *
 * Note: not suitable for multi-instance production — each instance has its own disk
 * and files are lost on redeploy. Use the `s3` backend there.
 */
@Component
@ConditionalOnProperty(name = ["app.storage.backend"], havingValue = "local", matchIfMissing = true)
class LocalFileStorage(
    @Value($$"${app.storage.local.dir:uploads}") private val dir: String,
) : FileStorage {

    private lateinit var baseDir: Path

    @PostConstruct
    fun init() {
        baseDir = Paths.get(dir).toAbsolutePath().normalize()
        Files.createDirectories(baseDir)
    }

    override fun put(key: String, bytes: ByteArray, contentType: String) {
        val target = resolveWithinBase(key)
            ?: throw IllegalArgumentException("Unsafe storage key: $key")
        Files.createDirectories(target.parent)
        Files.newOutputStream(target).use { it.write(bytes) }
    }

    override fun get(key: String): ByteArray? {
        val target = resolveWithinBase(key) ?: return null
        return if (Files.exists(target)) Files.readAllBytes(target) else null
    }

    override fun delete(key: String) {
        val target = resolveWithinBase(key) ?: return
        Files.deleteIfExists(target)
    }

    /** Resolves [key] under [baseDir], returning null if it would escape the base directory. */
    private fun resolveWithinBase(key: String): Path? {
        val resolved = baseDir.resolve(key).normalize()
        return if (resolved.startsWith(baseDir)) resolved else null
    }
}
