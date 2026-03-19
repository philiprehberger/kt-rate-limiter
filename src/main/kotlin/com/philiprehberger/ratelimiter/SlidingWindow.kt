package com.philiprehberger.ratelimiter

import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.time.Duration

/**
 * A sliding window rate limiter.
 *
 * Tracks timestamps of recent requests and allows new requests only if the number of
 * requests in the current sliding window is below the [limit].
 *
 * @param limit The maximum number of requests allowed within the window.
 * @param window The duration of the sliding window.
 */
public class SlidingWindow(
    private val limit: Int,
    private val window: Duration,
) : RateLimiter {

    private val timestamps = ConcurrentLinkedDeque<Long>()
    private val windowMs = window.inWholeMilliseconds

    override fun tryAcquire(permits: Int): Boolean {
        cleanup()
        // Synchronized to ensure atomicity of check-then-act
        synchronized(this) {
            if (timestamps.size + permits > limit) return false
            val now = System.currentTimeMillis()
            repeat(permits) { timestamps.addLast(now) }
            return true
        }
    }

    override suspend fun acquire(permits: Int) {
        while (!tryAcquire(permits)) {
            val oldest = timestamps.peekFirst()
            val waitMs = if (oldest != null) {
                (oldest + windowMs - System.currentTimeMillis()).coerceAtLeast(1)
            } else {
                1L
            }
            delay(waitMs)
        }
    }

    override fun info(): RateLimitInfo {
        cleanup()
        val now = System.currentTimeMillis()
        val oldest = timestamps.peekFirst()
        return RateLimitInfo(
            remaining = (limit - timestamps.size).coerceAtLeast(0),
            limit = limit,
            resetsAt = if (oldest != null) oldest + windowMs else now + windowMs,
        )
    }

    private fun cleanup() {
        val cutoff = System.currentTimeMillis() - windowMs
        while (true) {
            val head = timestamps.peekFirst() ?: break
            if (head <= cutoff) {
                timestamps.pollFirst()
            } else {
                break
            }
        }
    }
}
