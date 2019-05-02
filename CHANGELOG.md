# Changelog

## 0.1.6 (2026-03-22)

- Fix README compliance (badge label, installation format)

## 0.1.5 (2026-03-22)

- Reformat build.gradle.kts to standard multiline layout, standardize CHANGELOG

## 0.1.4 (2026-03-20)

- Standardize README: fix title, badges, version sync, remove Requirements section

## 0.1.3 (2026-03-20)

- Add issueManagement to POM metadata

## 0.1.2 (2026-03-18)

- Upgrade to Kotlin 2.0.21 and Gradle 8.12
- Enable explicitApi() for stricter public API surface
- Add issueManagement to POM metadata

## 0.1.1 (2026-03-18)

- Fix CI badge and gradlew permissions

## 0.1.0 (2026-03-17)

- `RateLimiter` interface with `tryAcquire()`, `acquire()`, and `info()`
- `TokenBucket` implementation with configurable rate, interval, and burst capacity
- `SlidingWindow` implementation using timestamp-based tracking
- `FixedWindow` implementation with boundary-based reset
- `KeyedRateLimiter` for per-key rate limiting with independent limiter instances
- `RateLimitInfo` data class for inspecting limiter state
- Thread-safe implementations using atomic operations and concurrent collections
