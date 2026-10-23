package com.xmh.checkin;

import static org.assertj.core.api.Assertions.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xmh.checkin.api.BusinessException;
import com.xmh.checkin.domain.CheckinPoint;
import com.xmh.checkin.domain.CheckinRecord;
import com.xmh.checkin.domain.Meeting;
import com.xmh.checkin.domain.MeetingRegistration;
import com.xmh.checkin.domain.OutboxEvent;
import com.xmh.checkin.mapper.CheckinPointMapper;
import com.xmh.checkin.mapper.CheckinRecordMapper;
import com.xmh.checkin.mapper.MeetingMapper;
import com.xmh.checkin.mapper.OutboxEventMapper;
import com.xmh.checkin.service.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CheckinFlowIntegrationTest {
  @Autowired AuthService auth;
  @Autowired MeetingService meetings;
  @Autowired RegistrationService registrations;
  @Autowired CheckinService checkins;
  @Autowired PointsEventHandler pointsHandler;
  @Autowired PointsService points;
  @Autowired OutboxEventMapper outbox;
  @Autowired MeetingMapper meetingMapper;
  @Autowired CheckinPointMapper pointMapper;
  @Autowired CheckinRecordMapper checkinRecordMapper;

  @Test
  void completeFlowAndDuplicateProtection() {
    AuthService.LoginResult alice = auth.login("dev-alice");
    assertThat(meetings.list()).hasSize(1);
    registrations.register(1, alice.userId());
    String token = meetings.detail(1).points().getFirst().entryToken();
    CheckinService.CheckinResult result =
        checkins.checkin(
            alice.userId(),
            new CheckinService.CheckinCommand(token, "QR", 31.03, 121.22, "127.0.0.1"),
            "trace-1");
    assertThat(result.distanceMeters()).isLessThanOrEqualTo(new BigDecimal("1.00"));
    OutboxEvent event =
        outbox.selectOne(
            new LambdaQueryWrapper<OutboxEvent>().eq(OutboxEvent::getEventId, result.eventId()));
    pointsHandler.handle(event.getPayload());
    pointsHandler.handle(event.getPayload());
    assertThat(points.balance(alice.userId())).isEqualTo(20);
    assertThat(points.ledger(alice.userId())).hasSize(1);
    assertThat(checkins.records(alice.userId())).hasSize(1);
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    alice.userId(),
                    new CheckinService.CheckinCommand(token, "NFC", 31.03, 121.22, "127.0.0.1"),
                    "trace-2"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("DUPLICATE_CHECKIN");

    CheckinRecord crossChannelDuplicate = new CheckinRecord();
    crossChannelDuplicate.setMeetingId(1L);
    crossChannelDuplicate.setUserId(alice.userId());
    crossChannelDuplicate.setPointId(1L);
    crossChannelDuplicate.setCheckinType("NFC");
    crossChannelDuplicate.setLatitude(new BigDecimal("31.0300000"));
    crossChannelDuplicate.setLongitude(new BigDecimal("121.2200000"));
    crossChannelDuplicate.setDistanceMeters(BigDecimal.ZERO);
    crossChannelDuplicate.setCheckedInAt(java.time.LocalDateTime.now());
    crossChannelDuplicate.setTraceId("trace-db-unique");
    assertThatThrownBy(() -> checkinRecordMapper.insert(crossChannelDuplicate))
        .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
  }

  @Test
  void requiresRegistrationAndEnforcesDistance() {
    AuthService.LoginResult bob = auth.login("dev-bob");
    String token = meetings.detail(1).points().getFirst().entryToken();
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    bob.userId(),
                    new CheckinService.CheckinCommand(token, "NFC", 31.03, 121.22, "127.0.0.2"),
                    "trace-3"))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("报名");
    registrations.register(1, bob.userId());
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    bob.userId(),
                    new CheckinService.CheckinCommand(token, "NFC", 30.00, 120.00, "127.0.0.2"),
                    "trace-4"))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("范围");
  }

  @Test
  void registrationIsIdempotentAndUserStateIsIsolated() {
    AuthService.LoginResult alice = auth.login("dev-alice");
    AuthService.LoginResult bob = auth.login("dev-bob");
    MeetingRegistration first = registrations.register(1, alice.userId());
    MeetingRegistration second = registrations.register(1, alice.userId());
    assertThat(second.getId()).isEqualTo(first.getId());
    assertThat(registrations.find(1, bob.userId())).isNull();
  }

  @Test
  void rejectsClosedPointInvalidCoordinateAndEntryType() {
    AuthService.LoginResult alice = auth.login("dev-alice");
    registrations.register(1, alice.userId());
    String token = meetings.detail(1).points().getFirst().entryToken();
    CheckinPoint point = pointMapper.selectById(1);
    point.setStatus("CLOSED");
    pointMapper.updateById(point);
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    alice.userId(),
                    new CheckinService.CheckinCommand(token, "NFC", 31.03, 121.22, "127.0.0.3"),
                    "trace-5"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("POINT_INVALID");
    point.setStatus("ACTIVE");
    pointMapper.updateById(point);
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    alice.userId(),
                    new CheckinService.CheckinCommand(token, "NFC", 91, 121.22, "127.0.0.3"),
                    "trace-6"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("LOCATION_INVALID");
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    alice.userId(),
                    new CheckinService.CheckinCommand(token, "BLE", 31.03, 121.22, "127.0.0.3"),
                    "trace-7"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("ENTRY_TYPE_INVALID");
  }

  @Test
  void rejectsBeforeStartAfterEndAndClosedMeeting() {
    AuthService.LoginResult bob = auth.login("dev-bob");
    registrations.register(1, bob.userId());
    String token = meetings.detail(1).points().getFirst().entryToken();
    Meeting meeting = meetingMapper.selectById(1);
    meeting.setCheckinStart(java.time.LocalDateTime.now().plusHours(1));
    meetingMapper.updateById(meeting);
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    bob.userId(),
                    new CheckinService.CheckinCommand(token, "NFC", 31.03, 121.22, "127.0.0.4"),
                    "trace-8"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("CHECKIN_NOT_STARTED");
    meeting.setCheckinStart(java.time.LocalDateTime.now().minusHours(2));
    meeting.setCheckinEnd(java.time.LocalDateTime.now().minusHours(1));
    meetingMapper.updateById(meeting);
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    bob.userId(),
                    new CheckinService.CheckinCommand(token, "NFC", 31.03, 121.22, "127.0.0.4"),
                    "trace-9"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("CHECKIN_ENDED");
    meeting.setCheckinEnd(java.time.LocalDateTime.now().plusHours(1));
    meeting.setStatus("CLOSED");
    meetingMapper.updateById(meeting);
    assertThatThrownBy(
            () ->
                checkins.checkin(
                    bob.userId(),
                    new CheckinService.CheckinCommand(token, "NFC", 31.03, 121.22, "127.0.0.4"),
                    "trace-10"))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo("MEETING_CLOSED");
  }
}
