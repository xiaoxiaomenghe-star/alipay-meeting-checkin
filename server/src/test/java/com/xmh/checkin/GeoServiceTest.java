package com.xmh.checkin;

import static org.assertj.core.api.Assertions.*;

import com.xmh.checkin.service.GeoService;
import org.junit.jupiter.api.Test;

class GeoServiceTest {
  @Test
  void calculatesDistance() {
    GeoService geo = new GeoService();
    assertThat(geo.distanceMeters(31.03, 121.22, 31.03, 121.22)).isZero();
    assertThat(geo.distanceMeters(31.03, 121.22, 31.04, 121.22)).isBetween(1100d, 1120d);
  }
}
