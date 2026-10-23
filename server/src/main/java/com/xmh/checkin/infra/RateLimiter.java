package com.xmh.checkin.infra;

public interface RateLimiter {
  boolean tryAcquire(String key, int limit, int windowSeconds);
}
