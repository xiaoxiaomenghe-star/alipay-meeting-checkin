package com.xmh.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xmh.checkin.api.BusinessException;
import com.xmh.checkin.domain.*;
import com.xmh.checkin.infra.IdempotencyStore;
import com.xmh.checkin.infra.RateLimiter;
import com.xmh.checkin.mapper.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckinService {
  private final MeetingService meetings;
  private final RegistrationService registrations;
  private final CheckinPointMapper points;
  private final CheckinRecordMapper records;
  private final OutboxEventMapper outbox;
  private final EntryTokenService entryTokens;
  private final GeoService geo;
  private final IdempotencyStore ids;
  private final RateLimiter limiter;
  private final ObjectMapper json;

  public CheckinService(
      MeetingService meetings,
      RegistrationService registrations,
      CheckinPointMapper points,
      CheckinRecordMapper records,
      OutboxEventMapper outbox,
      EntryTokenService entryTokens,
      GeoService geo,
      IdempotencyStore ids,
      RateLimiter limiter,
      ObjectMapper json) {
    this.meetings = meetings;
    this.registrations = registrations;
    this.points = points;
    this.records = records;
    this.outbox = outbox;
    this.entryTokens = entryTokens;
    this.geo = geo;
    this.ids = ids;
    this.limiter = limiter;
    this.json = json;
  }

  @Transactional
  public CheckinResult checkin(long userId, CheckinCommand command, String traceId) {
    String type = command.entryType().toUpperCase();
    if (!type.equals("QR") && !type.equals("NFC"))
      throw new BusinessException("ENTRY_TYPE_INVALID", "入口类型必须为QR或NFC", HttpStatus.BAD_REQUEST);
    EntryTokenService.EntryClaims claims = entryTokens.verify(command.entryToken());
    if (!limiter.tryAcquire("user:" + userId, 10, 60)
        || !limiter.tryAcquire("ip:" + command.clientIp(), 30, 60)
        || !limiter.tryAcquire("meeting:" + claims.meetingId(), 200, 60))
      throw new BusinessException("RATE_LIMITED", "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS);
    String idem = claims.meetingId() + ":" + userId;
    if (!ids.acquire(idem, Duration.ofMinutes(10)))
      throw new BusinessException("DUPLICATE_CHECKIN", "请勿重复签到", HttpStatus.CONFLICT);
    try {
      Meeting meeting = meetings.get(claims.meetingId());
      LocalDateTime now = LocalDateTime.now();
      if (!"OPEN".equals(meeting.getStatus()))
        throw new BusinessException("MEETING_CLOSED", "会议不可签到", HttpStatus.CONFLICT);
      if (now.isBefore(meeting.getCheckinStart()))
        throw new BusinessException("CHECKIN_NOT_STARTED", "签到尚未开始", HttpStatus.CONFLICT);
      if (now.isAfter(meeting.getCheckinEnd()))
        throw new BusinessException("CHECKIN_ENDED", "签到已经结束", HttpStatus.CONFLICT);
      registrations.require(meeting.getId(), userId);
      CheckinPoint point = points.selectById(claims.pointId());
      if (point == null
          || !point.getMeetingId().equals(meeting.getId())
          || !"ACTIVE".equals(point.getStatus()))
        throw new BusinessException("POINT_INVALID", "签到点无效", HttpStatus.CONFLICT);
      validateCoordinate(command.latitude(), command.longitude());
      double distance =
          geo.distanceMeters(
              command.latitude(),
              command.longitude(),
              point.getLatitude().doubleValue(),
              point.getLongitude().doubleValue());
      if (distance > point.getAllowedDistanceMeters())
        throw new BusinessException("OUT_OF_RANGE", "当前位置不在签到范围内", HttpStatus.CONFLICT);
      CheckinRecord record = new CheckinRecord();
      record.setMeetingId(meeting.getId());
      record.setUserId(userId);
      record.setPointId(point.getId());
      record.setCheckinType(type);
      record.setLatitude(BigDecimal.valueOf(command.latitude()));
      record.setLongitude(BigDecimal.valueOf(command.longitude()));
      record.setDistanceMeters(BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP));
      record.setCheckedInAt(now);
      record.setTraceId(traceId);
      try {
        records.insert(record);
      } catch (DuplicateKeyException ex) {
        throw new BusinessException("DUPLICATE_CHECKIN", "该入口已经签到", HttpStatus.CONFLICT);
      }
      String eventId = UUID.randomUUID().toString();
      PointsEvent event =
          new PointsEvent(eventId, userId, meeting.getId(), meeting.getPointsReward(), "会议签到积分");
      OutboxEvent box = new OutboxEvent();
      box.setEventId(eventId);
      box.setAggregateType("CHECKIN");
      box.setAggregateId(String.valueOf(record.getId()));
      box.setEventType("POINTS_AWARD");
      try {
        box.setPayload(json.writeValueAsString(event));
      } catch (Exception ex) {
        throw new IllegalStateException(ex);
      }
      box.setStatus("PENDING");
      box.setRetryCount(0);
      box.setNextRetryAt(now);
      box.setCreatedAt(now);
      outbox.insert(box);
      return new CheckinResult(
          record.getId(), eventId, record.getDistanceMeters(), meeting.getPointsReward());
    } catch (RuntimeException ex) {
      ids.release(idem);
      throw ex;
    }
  }

  public List<CheckinRecord> records(long userId) {
    return records.selectList(
        new LambdaQueryWrapper<CheckinRecord>()
            .eq(CheckinRecord::getUserId, userId)
            .orderByDesc(CheckinRecord::getCheckedInAt));
  }

  private void validateCoordinate(double lat, double lon) {
    if (lat < -90 || lat > 90 || lon < -180 || lon > 180)
      throw new BusinessException("LOCATION_INVALID", "定位坐标不合法", HttpStatus.BAD_REQUEST);
  }

  public record CheckinCommand(
      String entryToken, String entryType, double latitude, double longitude, String clientIp) {}

  public record CheckinResult(
      long recordId, String eventId, BigDecimal distanceMeters, int pointsPending) {}

  public record PointsEvent(
      String eventId, long userId, long meetingId, int delta, String reason) {}
}
