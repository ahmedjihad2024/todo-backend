package org.personal.todo.security.jwt

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [TokenBlacklist] for the `dev` profile — no Redis / Docker required.
 *
 * Revoked `jti`s are held in a [ConcurrentHashMap] mapping each to its expiry instant. Expiry is
 * checked lazily on read (an entry past its expiry is treated as not revoked and removed), so the map
 * never blocks the auth path on a sweep. State is per-instance and lost on restart, which is fine for
 * single-instance local development; production uses [RedisTokenBlacklist] for cluster-wide revocation.
 */
@Component
@Profile("dev")
class InMemoryTokenBlacklist : TokenBlacklist {

    private val revoked = ConcurrentHashMap<String, Instant>()

    override fun revoke(jti: String, expiresAt: Instant) {
        if (!expiresAt.isAfter(Instant.now())) return
        revoked[jti] = expiresAt
    }

    override fun isRevoked(jti: String): Boolean {
        val expiresAt = revoked[jti] ?: return false
        if (!expiresAt.isAfter(Instant.now())) {
            revoked.remove(jti, expiresAt)
            return false
        }
        return true
    }
}
