package com.xmh.checkin.auth;

import com.xmh.checkin.api.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class DevAuthProvider implements AuthProvider {
  @Override
  public Identity exchange(String authCode) {
    return switch (authCode) {
      case "dev-alice" -> new Identity("dev-alice", "Alice");
      case "dev-bob" -> new Identity("dev-bob", "Bob");
      default ->
          throw new BusinessException("AUTH_CODE_INVALID", "开发授权码无效", HttpStatus.UNAUTHORIZED);
    };
  }
}
