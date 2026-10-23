package com.xmh.checkin;

import static org.assertj.core.api.Assertions.*;

import com.xmh.checkin.infra.InMemoryGuards;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class InMemoryGuardsTest {
  @Test
  void idempotencyAndRateLimit() {
    InMemoryGuards g = new InMemoryGuards();
    assertThat(g.acquire("x", Duration.ofSeconds(2))).isTrue();
    assertThat(g.acquire("x", Duration.ofSeconds(2))).isFalse();
    g.release("x");
    assertThat(g.acquire("x", Duration.ofSeconds(2))).isTrue();
    assertThat(g.tryAcquire("r", 2, 10)).isTrue();
    assertThat(g.tryAcquire("r", 2, 10)).isTrue();
    assertThat(g.tryAcquire("r", 2, 10)).isFalse();
  }
}
