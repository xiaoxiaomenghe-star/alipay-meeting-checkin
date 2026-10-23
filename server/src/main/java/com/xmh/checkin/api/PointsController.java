package com.xmh.checkin.api;

import com.xmh.checkin.domain.PointsLedger;
import com.xmh.checkin.service.PointsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/points")
public class PointsController {
  private final PointsService service;

  public PointsController(PointsService service) {
    this.service = service;
  }

  @GetMapping
  public ApiResponse<Map<String, Integer>> balance(HttpServletRequest r) {
    return ApiResponse.ok(trace(r), Map.of("balance", service.balance(user(r))));
  }

  @GetMapping("/ledger")
  public ApiResponse<List<PointsLedger>> ledger(HttpServletRequest r) {
    return ApiResponse.ok(trace(r), service.ledger(user(r)));
  }

  private long user(HttpServletRequest r) {
    return (long) r.getAttribute("userId");
  }

  private String trace(HttpServletRequest r) {
    return String.valueOf(r.getAttribute("traceId"));
  }
}
