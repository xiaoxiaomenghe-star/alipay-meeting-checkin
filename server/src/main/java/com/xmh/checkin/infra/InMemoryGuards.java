package com.xmh.checkin.infra;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryGuards implements IdempotencyStore, RateLimiter {
  private final ConcurrentHashMap<String, Instant> ids = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

  @Override
  public boolean acquire(String key, Duration ttl) {
    Instant now = Instant.now();
    ids.entrySet().removeIf(e -> e.getValue().isBefore(now));
    return ids.putIfAbsent(key, now.plus(ttl)) == null;
  }

  @Override
  public void release(String key) {
    ids.remove(key);
  }

  @Override
  public boolean tryAcquire(String key, int limit, int seconds) {
    Instant now = Instant.now();
    Counter value =
        counters.compute(
            key,
            (k, v) ->
                v == null || v.expires.isBefore(now)
                    ? new Counter(new AtomicInteger(1), now.plusSeconds(seconds))
                    : new Counter(new AtomicInteger(v.count.incrementAndGet()), v.expires));
    return value.count.get() <= limit;
  }

  private record Counter(AtomicInteger count, Instant expires) {}
}
