package com.philiprehberger.ratelimiter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class RateLimiterTest {

    @Test
    fun `token bucket allows within rate`() {
        val limiter = TokenBucket(rate = 5, interval = 1.seconds, burst = 5)
        repeat(5) {
            assertTrue(limiter.tryAcquire(), "Attempt ${it + 1} should succeed")
        }
    }

    @Test
    fun `token bucket rejects over rate`() {
        val limiter = TokenBucket(rate = 3, interval = 1.seconds, burst = 3)
        repeat(3) { assertTrue(limiter.tryAcquire()) }
        assertFalse(limiter.tryAcquire(), "Should reject 4th request")
    }

    @Test
    fun `token bucket burst handling`() {
        val limiter = TokenBucket(rate = 2, interval = 1.seconds, burst = 5)
        // Burst capacity allows 5 at once
        repeat(5) {
            assertTrue(limiter.tryAcquire(), "Burst attempt ${it + 1} should succeed")
        }
        assertFalse(limiter.tryAcquire(), "Should reject after burst exhausted")
    }

    @Test
    fun `sliding window allows within limit`() {
        val limiter = SlidingWindow(limit = 3, window = 1.seconds)
        repeat(3) { assertTrue(limiter.tryAcquire()) }
        assertFalse(limiter.tryAcquire())
    }

    @Test
    fun `sliding window resets after window`() {
        val limiter = SlidingWindow(limit = 2, window = 50.milliseconds)
        repeat(2) { assertTrue(limiter.tryAcquire()) }
        assertFalse(limiter.tryAcquire())

        Thread.sleep(60)
        assertTrue(limiter.tryAcquire(), "Should allow after window expires")
    }

    @Test
    fun `fixed window allows within limit`() {
        val limiter = FixedWindow(limit = 3, window = 1.seconds)
        repeat(3) { assertTrue(limiter.tryAcquire()) }
        assertFalse(limiter.tryAcquire())
    }

    @Test
    fun `fixed window resets at boundary`() {
        val limiter = FixedWindow(limit = 2, window = 50.milliseconds)
        repeat(2) { assertTrue(limiter.tryAcquire()) }
        assertFalse(limiter.tryAcquire())

        Thread.sleep(60)
        assertTrue(limiter.tryAcquire(), "Should allow after window resets")
    }

    @Test
    fun `keyed limiter isolates per key`() {
        val keyed = KeyedRateLimiter<String> {
            TokenBucket(rate = 2, interval = 1.seconds, burst = 2)
        }
        // User A can make 2 requests
        assertTrue(keyed.tryAcquire("userA"))
        assertTrue(keyed.tryAcquire("userA"))
        assertFalse(keyed.tryAcquire("userA"))

        // User B is independent
        assertTrue(keyed.tryAcquire("userB"))
        assertTrue(keyed.tryAcquire("userB"))
        assertFalse(keyed.tryAcquire("userB"))
    }

    @Test
    fun `keyed limiter tracks key count`() {
        val keyed = KeyedRateLimiter<String> {
            TokenBucket(rate = 10, interval = 1.seconds)
        }
        keyed.tryAcquire("a")
        keyed.tryAcquire("b")
        keyed.tryAcquire("c")
        assertEquals(3, keyed.size)
    }

    @Test
    fun `acquire suspends until available`() = runTest {
        val limiter = TokenBucket(rate = 10, interval = 50.milliseconds, burst = 1)
        // Exhaust the bucket
        assertTrue(limiter.tryAcquire())
        assertFalse(limiter.tryAcquire())

        // acquire should eventually succeed after refill
        val job = async { limiter.acquire() }
        job.await() // Will succeed after tokens are refilled
    }

    @Test
    fun `info returns correct remaining count`() {
        val limiter = TokenBucket(rate = 5, interval = 1.seconds, burst = 5)
        assertEquals(5, limiter.info().remaining)
        limiter.tryAcquire(2)
        assertEquals(3, limiter.info().remaining)
        assertEquals(5, limiter.info().limit)
    }

    @Test
    fun `multiple permits acquisition`() {
        val limiter = TokenBucket(rate = 5, interval = 1.seconds, burst = 5)
        assertTrue(limiter.tryAcquire(3))
        assertTrue(limiter.tryAcquire(2))
        assertFalse(limiter.tryAcquire(1))
    }
}
