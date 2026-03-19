package com.philiprehberger.ratelimiter

import java.util.concurrent.ConcurrentHashMap

/**
 * A keyed rate limiter that maintains a separate [RateLimiter] per key.
 *
 * Useful for per-user, per-IP, or per-API-key rate limiting.
 *
 * @param K The key type.
 * @param factory Factory function that creates a new [RateLimiter] for each key.
 */
public class KeyedRateLimiter<K>(
    private val factory: () -> RateLimiter,
) {
    private val limiters = ConcurrentHashMap<K, RateLimiter>()

    /**
     * Attempts to acquire [permits] for the given [key] without blocking.
     *
     * @param key The key to acquire permits for.
     * @param permits The number of permits to acquire.
     * @return `true` if acquired, `false` if the rate limit for this key would be exceeded.
     */
    public fun tryAcquire(key: K, permits: Int = 1): Boolean {
        return getLimiter(key).tryAcquire(permits)
    }

    /**
     * Acquires [permits] for the given [key], suspending until available.
     *
     * @param key The key to acquire permits for.
     * @param permits The number of permits to acquire.
     */
    public suspend fun acquire(key: K, permits: Int = 1): Unit {
        getLimiter(key).acquire(permits)
    }

    /**
     * Returns rate limit info for the given [key].
     *
     * @param key The key to inspect.
     * @return A [RateLimitInfo] snapshot for the key's limiter.
     */
    public fun info(key: K): RateLimitInfo {
        return getLimiter(key).info()
    }

    /**
     * Returns the number of keys currently tracked.
     */
    public val size: Int get() = limiters.size

    private fun getLimiter(key: K): RateLimiter {
        return limiters.computeIfAbsent(key) { factory() }
    }
}
