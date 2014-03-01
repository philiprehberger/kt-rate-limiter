package com.philiprehberger.ratelimiter

/**
 * Information about the current state of a rate limiter.
 *
 * @property remaining The number of permits currently available.
 * @property limit The maximum number of permits in the current window or bucket.
 * @property resetsAt The epoch millisecond timestamp when permits will next be replenished or the window resets.
 */
data class RateLimitInfo(
    val remaining: Int,
    val limit: Int,
    val resetsAt: Long,
)
