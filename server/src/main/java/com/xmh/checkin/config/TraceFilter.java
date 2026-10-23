package com.xmh.checkin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String traceId = request.getHeader("X-Request-Id");
    if (traceId == null || traceId.isBlank())
      traceId = UUID.randomUUID().toString().replace("-", "");
    request.setAttribute("traceId", traceId);
    response.setHeader("X-Request-Id", traceId);
    chain.doFilter(request, response);
  }
}
