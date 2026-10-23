package com.xmh.checkin.api;

import com.xmh.checkin.domain.Meeting;
import com.xmh.checkin.domain.MeetingRegistration;
import com.xmh.checkin.service.MeetingService;
import com.xmh.checkin.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/meetings")
public class MeetingController {
  private final MeetingService meetings;
  private final RegistrationService registrations;

  public MeetingController(MeetingService meetings, RegistrationService registrations) {
    this.meetings = meetings;
    this.registrations = registrations;
  }

  @GetMapping
  public ApiResponse<List<Meeting>> list(HttpServletRequest r) {
    return ApiResponse.ok(trace(r), meetings.list());
  }

  @GetMapping("/{id}")
  public ApiResponse<MeetingService.MeetingDetail> detail(
      @PathVariable long id, HttpServletRequest r) {
    return ApiResponse.ok(trace(r), meetings.detail(id));
  }

  @PostMapping("/{id}/registrations")
  public ApiResponse<MeetingRegistration> register(@PathVariable long id, HttpServletRequest r) {
    return ApiResponse.ok(trace(r), registrations.register(id, user(r)));
  }

  @GetMapping("/{id}/registration")
  public ApiResponse<MeetingRegistration> registration(
      @PathVariable long id, HttpServletRequest r) {
    return ApiResponse.ok(trace(r), registrations.find(id, user(r)));
  }

  private long user(HttpServletRequest r) {
    return (long) r.getAttribute("userId");
  }

  private String trace(HttpServletRequest r) {
    return String.valueOf(r.getAttribute("traceId"));
  }
}
