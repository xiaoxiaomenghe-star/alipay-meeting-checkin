package com.xmh.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xmh.checkin.api.BusinessException;
import com.xmh.checkin.domain.CheckinPoint;
import com.xmh.checkin.domain.Meeting;
import com.xmh.checkin.mapper.CheckinPointMapper;
import com.xmh.checkin.mapper.MeetingMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MeetingService {
  private final MeetingMapper meetings;
  private final CheckinPointMapper points;
  private final EntryTokenService tokens;

  public MeetingService(
      MeetingMapper meetings, CheckinPointMapper points, EntryTokenService tokens) {
    this.meetings = meetings;
    this.points = points;
    this.tokens = tokens;
  }

  public List<Meeting> list() {
    return meetings.selectList(
        new LambdaQueryWrapper<Meeting>().orderByAsc(Meeting::getCheckinStart));
  }

  public MeetingDetail detail(long meetingId) {
    Meeting meeting = get(meetingId);
    List<PointView> views =
        points
            .selectList(
                new LambdaQueryWrapper<CheckinPoint>().eq(CheckinPoint::getMeetingId, meetingId))
            .stream()
            .map(
                p ->
                    new PointView(
                        p.getId(),
                        p.getPointCode(),
                        p.getPointName(),
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getAllowedDistanceMeters(),
                        p.getStatus(),
                        tokens.issue(meetingId, p.getId())))
            .toList();
    return new MeetingDetail(meeting, views);
  }

  public Meeting get(long id) {
    Meeting value = meetings.selectById(id);
    if (value == null)
      throw new BusinessException("MEETING_NOT_FOUND", "会议不存在", HttpStatus.NOT_FOUND);
    return value;
  }

  public record PointView(
      Long id,
      String pointCode,
      String pointName,
      java.math.BigDecimal latitude,
      java.math.BigDecimal longitude,
      Integer allowedDistanceMeters,
      String status,
      String entryToken) {}

  public record MeetingDetail(Meeting meeting, List<PointView> points) {}
}
