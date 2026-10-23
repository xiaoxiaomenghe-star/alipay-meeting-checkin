package com.xmh.checkin.service;

import org.springframework.stereotype.Service;

@Service
public class GeoService {
  public double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
    double earth = 6371000D;
    double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    return earth * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}
