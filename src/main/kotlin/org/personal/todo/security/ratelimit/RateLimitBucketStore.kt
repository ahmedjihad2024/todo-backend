package org.personal.todo.security.ratelimit

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

/**
 * Resolves the token bucket for a given rate-limit key. Abstracts over *where* buckets live so the
 * [RateLimitFilter] doesn't care whether the store is Redis (shared across replicas) or a local map.
 *
 * Two implementations are wired by profile:
 * - prod ([RedisRateLimitBucketStore]): bucket4j's Redis-backed proxy manager — one shared bucket per
 *   key across every replica, so the limit is enforced globally.
 * - dev ([InMemoryRateLimitBucketStore]): a per-instance [ConcurrentHashMap] of local buckets — no
 *   Redis / Docker required.
 */
interface RateLimitBucketStore {
    fun resolveBucket(key: String, config: Supplier<BucketConfiguration>): Bucket
}

/** prod: delegates to the Redis-backed bucket4j proxy manager (see RedisConfig). */
@Component
@Profile("!dev")
class RedisRateLimitBucketStore(
    private val proxyManager: LettuceBasedProxyManager<String>,
) : RateLimitBucketStore {

    override fun resolveBucket(key: String, config: Supplier<BucketConfiguration>): Bucket =
        proxyManager.builder().build(key, config)
}

/**
 * dev: holds one local bucket4j bucket per key in a [ConcurrentHashMap]. The buckets self-refill, so
 * no TTL / eviction is needed; the map only grows with the set of distinct keys seen this run, which
 * is bounded enough for local development. State is per-instance and lost on restart.
 */
@Component
@Profile("dev")
class InMemoryRateLimitBucketStore : RateLimitBucketStore {

    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun resolveBucket(key: String, config: Supplier<BucketConfiguration>): Bucket =
        buckets.computeIfAbsent(key) {
            val builder = Bucket.builder()
            config.get().bandwidths.forEach { builder.addLimit(it) }
            builder.build()
        }
}
