package com.xmh.checkin.infra;

import java.time.Duration;

public interface IdempotencyStore {
  boolean acquire(String key, Duration ttl);

  void release(String key);
}
