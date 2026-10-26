package org.personal.todo.security.jwt

import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * Cluster-wide blacklist of revoked access tokens, keyed by the token's `jti`.
 *
 * On logout (or any revocation) the `jti` is recorded with a lifetime equal to the token's *remaining*
 * lifetime, so the entry self-expires exactly when the token would have anyway — no sweep job, no
 * growth beyond the set of still-valid revoked tokens. A token revoked on one instance is rejected
 * everywhere it's visible (see [JwtAuthFilter]).
 *
 * Two implementations are wired by profile:
 * - prod ([RedisTokenBlacklist]): Redis-backed, so the blacklist is shared across every replica.
 * - dev ([InMemoryTokenBlacklist]): a single-instance in-memory map — no Redis / Docker required.
 */
interface TokenBlacklist {

    /**
     * Revoke the token with this [jti] until [expiresAt]. No-op if the token has already expired
     * (nothing to block — it would fail validation anyway).
     */
    fun revoke(jti: String, expiresAt: Instant)

    /** True if the token with this [jti] has been revoked and is still within its lifetime. */
    fun isRevoked(jti: String): Boolean
}

/**
 * Redis-backed blacklist. The `jti` is written under [key] with a TTL equal to the token's remaining
 * lifetime, so Redis' native per-key expiry removes it exactly when the token expires. Every replica
 * reads the same Redis, so revocation is cluster-wide and the lookup is an O(1) key check on the hot
 * auth path.
 *
 * Replaces the previous MongoDB `revoked_access_tokens` collection: Redis' native per-key TTL is a
 * better fit than a Mongo TTL index.
 */
@Component
@Profile("!dev")
class RedisTokenBlacklist(
    private val redis: StringRedisTemplate,
) : TokenBlacklist {

    private companion object {
        fun key(jti: String) = "blacklist:jti:$jti"
    }

    override fun revoke(jti: String, expiresAt: Instant) {
        val ttl = Duration.between(Instant.now(), expiresAt)
        if (ttl.isZero || ttl.isNegative) return
        redis.opsForValue().set(key(jti), "1", ttl)
    }

    override fun isRevoked(jti: String): Boolean = redis.hasKey(key(jti))
}
