package com.xmh.checkin.auth;

import com.xmh.checkin.api.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SessionTokenService {
  private final byte[] secret;

  public SessionTokenService(@Value("${app.auth-secret}") String secret) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
  }

  public String issue(long userId) {
    long expires = Instant.now().plusSeconds(86400).getEpochSecond();
    String body = userId + ":" + expires;
    return enc(body) + "." + enc(sign(body));
  }

  public long verify(String token) {
    try {
      String[] parts = token.split("\\.");
      String body = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
      if (!constantTime(parts[1], enc(sign(body)))) throw new IllegalArgumentException();
      String[] values = body.split(":");
      if (Instant.now().getEpochSecond() > Long.parseLong(values[1]))
        throw new IllegalArgumentException();
      return Long.parseLong(values[0]);
    } catch (Exception ex) {
      throw new BusinessException("AUTH_TOKEN_INVALID", "登录状态无效或已过期", HttpStatus.UNAUTHORIZED);
    }
  }

  private byte[] sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private String enc(String value) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  private String enc(byte[] value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
  }

  private boolean constantTime(String a, String b) {
    return java.security.MessageDigest.isEqual(
        a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
  }
}
