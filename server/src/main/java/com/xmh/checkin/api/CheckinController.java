package com.xmh.checkin.api;

import com.xmh.checkin.domain.CheckinRecord;
import com.xmh.checkin.service.CheckinService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CheckinController {
  private final CheckinService service;

  public CheckinController(CheckinService service) {
    this.service = service;
  }

  @PostMapping("/check-ins")
  public ApiResponse<CheckinService.CheckinResult> checkin(
      @Valid @RequestBody CheckinRequest body, HttpServletRequest r) {
    return ApiResponse.ok(
        trace(r),
        service.checkin(
            user(r),
            new CheckinService.CheckinCommand(
                body.entryToken(),
                body.entryType(),
                body.latitude(),
                body.longitude(),
                clientIp(r)),
            trace(r)));
  }

  @GetMapping("/me/check-ins")
  public ApiResponse<List<CheckinRecord>> records(HttpServletRequest r) {
    return ApiResponse.ok(trace(r), service.records(user(r)));
  }

  public record CheckinRequest(
      @NotBlank String entryToken,
      @NotBlank String entryType,
      @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
      @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}

  private long user(HttpServletRequest r) {
    return (long) r.getAttribute("userId");
  }

  private String trace(HttpServletRequest r) {
    return String.valueOf(r.getAttribute("traceId"));
  }

  private String clientIp(HttpServletRequest r) {
    String forwarded = r.getHeader("X-Forwarded-For");
    return forwarded == null || forwarded.isBlank()
        ? r.getRemoteAddr()
        : forwarded.split(",")[0].trim();
  }
}
