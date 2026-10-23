package com.xmh.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xmh.checkin.auth.AuthProvider;
import com.xmh.checkin.auth.SessionTokenService;
import com.xmh.checkin.domain.AppUser;
import com.xmh.checkin.mapper.AppUserMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final AuthProvider provider;
  private final SessionTokenService tokens;
  private final AppUserMapper users;

  public AuthService(AuthProvider provider, SessionTokenService tokens, AppUserMapper users) {
    this.provider = provider;
    this.tokens = tokens;
    this.users = users;
  }

  public LoginResult login(String code) {
    AuthProvider.Identity identity = provider.exchange(code);
    AppUser user =
        users.selectOne(
            new LambdaQueryWrapper<AppUser>().eq(AppUser::getExternalId, identity.externalId()));
    if (user == null) {
      user = new AppUser();
      user.setExternalId(identity.externalId());
      user.setDisplayName(identity.displayName());
      user.setPointsBalance(0);
      user.setCreatedAt(LocalDateTime.now());
      user.setUpdatedAt(LocalDateTime.now());
      users.insert(user);
    }
    return new LoginResult(tokens.issue(user.getId()), user.getId(), user.getDisplayName());
  }

  public record LoginResult(String token, long userId, String displayName) {}
}
