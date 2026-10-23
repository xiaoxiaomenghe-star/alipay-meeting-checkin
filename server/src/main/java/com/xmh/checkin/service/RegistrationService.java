package com.xmh.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xmh.checkin.api.BusinessException;
import com.xmh.checkin.domain.Meeting;
import com.xmh.checkin.domain.MeetingRegistration;
import com.xmh.checkin.mapper.MeetingRegistrationMapper;
import java.time.LocalDateTime;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
  private final MeetingRegistrationMapper registrations;
  private final MeetingService meetings;

  public RegistrationService(MeetingRegistrationMapper registrations, MeetingService meetings) {
    this.registrations = registrations;
    this.meetings = meetings;
  }

  public MeetingRegistration register(long meetingId, long userId) {
    Meeting meeting = meetings.get(meetingId);
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(meeting.getRegistrationStart()) || now.isAfter(meeting.getRegistrationEnd()))
      throw new BusinessException("REGISTRATION_CLOSED", "当前不在报名时间内", HttpStatus.CONFLICT);
    MeetingRegistration existing = find(meetingId, userId);
    if (existing != null) return existing;
    MeetingRegistration value = new MeetingRegistration();
    value.setMeetingId(meetingId);
    value.setUserId(userId);
    value.setStatus("REGISTERED");
    value.setRegisteredAt(now);
    try {
      registrations.insert(value);
      return value;
    } catch (DuplicateKeyException ex) {
      return find(meetingId, userId);
    }
  }

  public MeetingRegistration require(long meetingId, long userId) {
    MeetingRegistration value = find(meetingId, userId);
    if (value == null || !"REGISTERED".equals(value.getStatus()))
      throw new BusinessException("NOT_REGISTERED", "请先完成会议报名", HttpStatus.CONFLICT);
    return value;
  }

  public MeetingRegistration find(long meetingId, long userId) {
    return registrations.selectOne(
        new LambdaQueryWrapper<MeetingRegistration>()
            .eq(MeetingRegistration::getMeetingId, meetingId)
            .eq(MeetingRegistration::getUserId, userId));
  }
}
