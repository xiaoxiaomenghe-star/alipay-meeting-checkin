package com.xmh.checkin.infra;

import java.time.Duration;
import java.util.Collections;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RedisGuards implements IdempotencyStore, RateLimiter {
  private final StringRedisTemplate redis;
  private final DefaultRedisScript<Long> limitScript;

  public RedisGuards(StringRedisTemplate redis) {
    this.redis = redis;
    this.limitScript =
        new DefaultRedisScript<>(
            "local c=redis.call('INCR',KEYS[1]); if c==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]); end; return c",
            Long.class);
  }

  @Override
  public boolean acquire(String key, Duration ttl) {
    return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent("idem:" + key, "1", ttl));
  }

  @Override
  public void release(String key) {
    redis.delete("idem:" + key);
  }

  @Override
  public boolean tryAcquire(String key, int limit, int windowSeconds) {
    Long count =
        redis.execute(
            limitScript, Collections.singletonList("rate:" + key), String.valueOf(windowSeconds));
    return count != null && count <= limit;
  }
}
