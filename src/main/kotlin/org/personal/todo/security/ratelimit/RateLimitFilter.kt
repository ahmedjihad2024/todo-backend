package org.personal.todo.security.ratelimit

import org.personal.todo.common.dto.ApiResponse
import org.personal.todo.common.exception.ErrorCode
import org.personal.todo.common.extentions.tr
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper

@Component
class RateLimitFilter(
    private val properties: RateLimitProperties,
    private val objectMapper: ObjectMapper,
    /**
     * Bucket store. Backed by Redis in prod (one shared bucket per key across replicas) or an
     * in-memory map in dev — see [RateLimitBucketStore].
     */
    private val bucketStore: RateLimitBucketStore,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!properties.enabled) {
            filterChain.doFilter(request, response)
            return
        }

        val resolution = resolve(request) ?: run {
            filterChain.doFilter(request, response)
            return
        }

        val probe = try {
            val bucket = bucketStore.resolveBucket(resolution.key) { bucketConfig(resolution.rule) }
            bucket.tryConsumeAndReturnRemaining(1)
        } catch (e: Exception) {
            // Fail-open: a store outage (e.g. Redis down in prod) must not lock real users out of the
            // whole API. We lose throttling for the duration of the outage, the safer trade-off here.
            log.warn("Rate limit check skipped — bucket store unavailable: {}", e.message)
            filterChain.doFilter(request, response)
            return
        }

        if (!probe.isConsumed) {
            writeTooManyRequests(response, probe.nanosToWaitForRefill / 1_000_000_000L)
            return
        }

        response.setHeader("X-RateLimit-Remaining", probe.remainingTokens.toString())
        filterChain.doFilter(request, response)
    }

    private fun resolve(request: HttpServletRequest): Resolution? {
        val path = request.requestURI
        val method = request.method
        val ip = clientIp(request)

        return when {
            method == "POST" && path == "/api/auth/login" ->
                Resolution("login:$ip", properties.login)
            method == "POST" && path == "/api/auth/register" ->
                Resolution("register:$ip", properties.register)
            method == "POST" && path == "/api/auth/refresh" ->
                Resolution("refresh:$ip", properties.refresh)
            method == "POST" && path == "/api/admin/auth/login" ->
                Resolution("admin-login:$ip", properties.adminLogin)
            method == "POST" && path == "/api/admin/auth/register" ->
                Resolution("admin-register:$ip", properties.adminRegister)
            path.startsWith("/api/") -> {
                val key = authenticatedKeyOrIp(request, ip)
                Resolution(key, properties.authenticated)
            }
            else -> null
        }
    }

    private fun authenticatedKeyOrIp(request: HttpServletRequest, ip: String): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val principalName = authentication?.name
        if (!principalName.isNullOrBlank()) {
            return "user:$principalName"
        }

        return "ip:$ip"
    }


    private fun bucketConfig(rule: RateLimitProperties.Rule): BucketConfiguration {
        val limit = Bandwidth.builder()
            .capacity(rule.capacity)
            .refillIntervally(rule.capacity, rule.refill)
            .build()
        return BucketConfiguration.builder().addLimit(limit).build()
    }

    private fun clientIp(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        if (!forwarded.isNullOrBlank() && isTrustedProxy(request)) {
            return forwarded.split(",").first().trim()
        }
        return request.remoteAddr ?: "unknown"
    }

    private fun isTrustedProxy(request: HttpServletRequest): Boolean {
        val remoteAddr = request.remoteAddr ?: return false
        val trustedProxyIps = setOf("127.0.0.1", "0.0.0.0", "::1")
        return remoteAddr in trustedProxyIps || remoteAddr.startsWith("127.") || remoteAddr.startsWith("::1")
    }

    private fun writeTooManyRequests(response: HttpServletResponse, retryAfterSeconds: Long) {
        response.status = 429
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        if (retryAfterSeconds > 0) {
            response.setHeader("Retry-After", retryAfterSeconds.toString())
        }
        val body = ApiResponse.fail(ErrorCode.RATE_LIMIT_EXCEEDED.code, "error.rate_limit.exceeded".tr())
        objectMapper.writeValue(response.outputStream, body)
    }

    private data class Resolution(val key: String, val rule: RateLimitProperties.Rule)
}
