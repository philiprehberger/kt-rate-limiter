# rate-limiter

[![CI](https://github.com/philiprehberger/kt-rate-limiter/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-rate-limiter/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/rate-limiter)](https://central.sonatype.com/artifact/com.philiprehberger/rate-limiter)
[![License](https://img.shields.io/github/license/philiprehberger/kt-rate-limiter)](LICENSE)

Coroutine-native rate limiting with token bucket, sliding window, and fixed window algorithms.

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.philiprehberger:rate-limiter:0.1.4")
}
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>rate-limiter</artifactId>
    <version>0.1.4</version>
</dependency>
```

## Usage

### Token Bucket

```kotlin
import com.philiprehberger.ratelimiter.*
import kotlin.time.Duration.Companion.seconds

val limiter = TokenBucket(rate = 10, interval = 1.seconds, burst = 20)

if (limiter.tryAcquire()) {
    processRequest()
}

// Or suspend until a permit is available
limiter.acquire()
processRequest()
```

### Sliding Window

```kotlin
val limiter = SlidingWindow(limit = 100, window = 60.seconds)

if (limiter.tryAcquire()) {
    handleRequest()
}
```

### Fixed Window

```kotlin
val limiter = FixedWindow(limit = 1000, window = 60.seconds)

if (limiter.tryAcquire()) {
    handleRequest()
}
```

### Per-Key Rate Limiting

```kotlin
val perUser = KeyedRateLimiter<String> {
    TokenBucket(rate = 10, interval = 1.seconds, burst = 20)
}

if (perUser.tryAcquire(userId)) {
    handleRequest(userId)
}
```

### Rate Limit Info

```kotlin
val info = limiter.info()
println("Remaining: ${info.remaining}/${info.limit}")
println("Resets at: ${info.resetsAt}")
```

## API

| Class / Function | Description |
|------------------|-------------|
| `RateLimiter` | Interface with `tryAcquire()`, `acquire()`, and `info()` |
| `TokenBucket` | Token bucket algorithm with configurable rate, interval, and burst |
| `SlidingWindow` | Sliding window counter using timestamp deque |
| `FixedWindow` | Fixed window counter that resets at boundaries |
| `KeyedRateLimiter<K>` | Per-key rate limiting with independent limiter instances |
| `RateLimitInfo` | Data class with remaining permits, limit, and reset time |

## Development

```bash
./gradlew test       # Run tests
./gradlew check      # Run all checks
./gradlew build      # Build JAR
```

## License

MIT
