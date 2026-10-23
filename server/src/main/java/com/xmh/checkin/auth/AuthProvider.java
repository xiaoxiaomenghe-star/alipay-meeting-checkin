package com.xmh.checkin.auth;

public interface AuthProvider {
  Identity exchange(String authCode);

  record Identity(String externalId, String displayName) {}
}
