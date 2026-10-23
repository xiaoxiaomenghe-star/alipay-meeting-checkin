package com.xmh.checkin.service;

import com.xmh.checkin.api.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EntryTokenService {
  private final byte[] secret;

  public EntryTokenService(@Value("${app.entry-secret}") String secret) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
  }

  public String issue(long meetingId, long pointId) {
    String body =
        meetingId
            + ":"
            + pointId
            + ":"
            + Instant.now().plusSeconds(86400).getEpochSecond()
            + ":"
            + UUID.randomUUID();
    return enc(body.getBytes(StandardCharsets.UTF_8)) + "." + enc(sign(body));
  }

  public EntryClaims verify(String token) {
    try {
      String[] parts = token.split("\\.");
      String body = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
      if (!java.security.MessageDigest.isEqual(
          parts[1].getBytes(StandardCharsets.UTF_8),
          enc(sign(body)).getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException();
      String[] values = body.split(":");
      long expires = Long.parseLong(values[2]);
      if (Instant.now().getEpochSecond() > expires)
        throw new BusinessException("ENTRY_TOKEN_EXPIRED", "入口令牌已过期", HttpStatus.BAD_REQUEST);
      return new EntryClaims(
          Long.parseLong(values[0]), Long.parseLong(values[1]), expires, values[3]);
    } catch (BusinessException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new BusinessException("ENTRY_TOKEN_INVALID", "入口令牌无效", HttpStatus.BAD_REQUEST);
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

  private String enc(byte[] value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
  }

  public record EntryClaims(long meetingId, long pointId, long expiresAt, String nonce) {}
}
