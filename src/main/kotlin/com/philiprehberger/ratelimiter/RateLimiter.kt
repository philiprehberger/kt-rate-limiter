package com.philiprehberger.ratelimiter

/**
 * A rate limiter that controls the rate of operations.
 *
 * Implementations provide different algorithms (token bucket, sliding window, fixed window).
 */
interface RateLimiter {

    /**
     * Attempts to acquire [permits] without blocking.
     *
     * @param permits The number of permits to acquire.
     * @return `true` if the permits were acquired, `false` if the rate limit would be exceeded.
     */
    fun tryAcquire(permits: Int = 1): Boolean

    /**
     * Acquires [permits], suspending if necessary until they become available.
     *
     * @param permits The number of permits to acquire.
     */
    suspend fun acquire(permits: Int = 1)

    /**
     * Returns information about the current state of this rate limiter.
     *
     * @return A [RateLimitInfo] snapshot.
     */
    fun info(): RateLimitInfo
}
