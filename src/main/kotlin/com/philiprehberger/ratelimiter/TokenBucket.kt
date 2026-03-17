package com.philiprehberger.ratelimiter

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

/**
 * A token bucket rate limiter.
 *
 * Tokens are added at a fixed rate ([rate] tokens per [interval]). Tokens accumulate
 * up to [burst] capacity. Each operation consumes one or more tokens.
 *
 * Thread-safe via atomic operations.
 *
 * @param rate The number of tokens added per interval.
 * @param interval The interval at which tokens are replenished.
 * @param burst The maximum number of tokens the bucket can hold.
 */
class TokenBucket(
    private val rate: Int,
    private val interval: Duration,
    private val burst: Int = rate,
) : RateLimiter {

    private val availableTokens = AtomicLong(burst.toLong())
    private val lastRefillTime = AtomicLong(System.currentTimeMillis())
    private val intervalMs = interval.inWholeMilliseconds

    override fun tryAcquire(permits: Int): Boolean {
        refill()
        while (true) {
            val current = availableTokens.get()
            if (current < permits) return false
            if (availableTokens.compareAndSet(current, current - permits)) return true
        }
    }

    override suspend fun acquire(permits: Int) {
        while (!tryAcquire(permits)) {
            // Estimate wait time based on token refill rate
            val tokensNeeded = permits - availableTokens.get()
            val waitMs = if (tokensNeeded > 0) {
                (tokensNeeded * intervalMs) / rate
            } else {
                1L
            }
            delay(waitMs.coerceAtLeast(1))
        }
    }

    override fun info(): RateLimitInfo {
        refill()
        return RateLimitInfo(
            remaining = availableTokens.get().toInt(),
            limit = burst,
            resetsAt = lastRefillTime.get() + intervalMs,
        )
    }

    private fun refill() {
        val now = System.currentTimeMillis()
        val last = lastRefillTime.get()
        val elapsed = now - last
        if (elapsed < intervalMs) return

        val intervals = elapsed / intervalMs
        val tokensToAdd = intervals * rate
        if (tokensToAdd > 0 && lastRefillTime.compareAndSet(last, last + intervals * intervalMs)) {
            while (true) {
                val current = availableTokens.get()
                val newValue = (current + tokensToAdd).coerceAtMost(burst.toLong())
                if (availableTokens.compareAndSet(current, newValue)) break
            }
        }
    }
}
