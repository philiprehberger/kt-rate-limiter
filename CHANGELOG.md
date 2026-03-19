# Changelog

All notable changes to this library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.2] - 2026-03-18

- Upgrade to Kotlin 2.0.21 and Gradle 8.12
- Enable explicitApi() for stricter public API surface
- Add issueManagement to POM metadata

## [Unreleased]

## [0.1.1] - 2026-03-18

- Fix CI badge and gradlew permissions

## [0.1.0] - 2026-03-17

### Added
- `RateLimiter` interface with `tryAcquire()`, `acquire()`, and `info()`
- `TokenBucket` implementation with configurable rate, interval, and burst capacity
- `SlidingWindow` implementation using timestamp-based tracking
- `FixedWindow` implementation with boundary-based reset
- `KeyedRateLimiter` for per-key rate limiting with independent limiter instances
- `RateLimitInfo` data class for inspecting limiter state
- Thread-safe implementations using atomic operations and concurrent collections
