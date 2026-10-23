package com.xmh.checkin;

import static org.assertj.core.api.Assertions.*;

import com.xmh.checkin.api.BusinessException;
import com.xmh.checkin.service.EntryTokenService;
import org.junit.jupiter.api.Test;

class EntryTokenServiceTest {
  private final EntryTokenService service = new EntryTokenService("test-entry-secret");

  @Test
  void roundTripAndTamperDetection() {
    String token = service.issue(3, 7);
    assertThat(service.verify(token).meetingId()).isEqualTo(3);
    assertThat(service.verify(token).pointId()).isEqualTo(7);
    assertThatThrownBy(() -> service.verify(token + "x")).isInstanceOf(BusinessException.class);
  }
}
