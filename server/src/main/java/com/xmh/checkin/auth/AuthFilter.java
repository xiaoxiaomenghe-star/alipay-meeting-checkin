package com.xmh.checkin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xmh.checkin.api.ApiResponse;
import com.xmh.checkin.api.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AuthFilter extends OncePerRequestFilter {
  private final SessionTokenService tokens;
  private final ObjectMapper json;

  public AuthFilter(SessionTokenService tokens, ObjectMapper json) {
    this.tokens = tokens;
    this.json = json;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.equals("/api/v1/auth/login") || path.startsWith("/actuator/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      String header = request.getHeader("Authorization");
      if (header == null || !header.startsWith("Bearer "))
        throw new BusinessException("AUTH_REQUIRED", "请先登录", HttpStatus.UNAUTHORIZED);
      request.setAttribute("userId", tokens.verify(header.substring(7)));
      chain.doFilter(request, response);
    } catch (BusinessException ex) {
      response.setStatus(ex.status().value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      json.writeValue(
          response.getWriter(),
          new ApiResponse<>(
              ex.code(), ex.getMessage(), String.valueOf(request.getAttribute("traceId")), null));
    }
  }
}
