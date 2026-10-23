package com.xmh.checkin.api;

import com.xmh.checkin.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/login")
  public ApiResponse<AuthService.LoginResult> login(
      @Valid @RequestBody LoginRequest body, HttpServletRequest request) {
    return ApiResponse.ok(trace(request), service.login(body.authCode()));
  }

  public record LoginRequest(@NotBlank String authCode) {}

  private String trace(HttpServletRequest r) {
    return String.valueOf(r.getAttribute("traceId"));
  }
}
