package com.philiprehberger.ratelimiter

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

/**
 * A fixed window rate limiter.
 *
 * Allows up to [limit] requests per window. The counter resets at the start of each window boundary.
 *
 * @param limit The maximum number of requests per window.
 * @param window The duration of each fixed window.
 */
class FixedWindow(
    private val limit: Int,
    private val window: Duration,
) : RateLimiter {

    private val windowMs = window.inWholeMilliseconds
    private val counter = AtomicInteger(0)
    private val windowStart = AtomicLong(System.currentTimeMillis())

    override fun tryAcquire(permits: Int): Boolean {
        resetIfNeeded()
        while (true) {
            val current = counter.get()
            if (current + permits > limit) return false
            if (counter.compareAndSet(current, current + permits)) return true
        }
    }

    override suspend fun acquire(permits: Int) {
        while (!tryAcquire(permits)) {
            val start = windowStart.get()
            val waitMs = (start + windowMs - System.currentTimeMillis()).coerceAtLeast(1)
            delay(waitMs)
        }
    }

    override fun info(): RateLimitInfo {
        resetIfNeeded()
        val start = windowStart.get()
        return RateLimitInfo(
            remaining = (limit - counter.get()).coerceAtLeast(0),
            limit = limit,
            resetsAt = start + windowMs,
        )
    }

    private fun resetIfNeeded() {
        val now = System.currentTimeMillis()
        val start = windowStart.get()
        if (now - start >= windowMs) {
            if (windowStart.compareAndSet(start, now)) {
                counter.set(0)
            }
        }
    }
}
