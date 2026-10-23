package com.xmh.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xmh.checkin.domain.AppUser;
import com.xmh.checkin.domain.PointsLedger;
import com.xmh.checkin.mapper.AppUserMapper;
import com.xmh.checkin.mapper.PointsLedgerMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsEventHandler {
  private final PointsLedgerMapper ledgers;
  private final AppUserMapper users;
  private final ObjectMapper json;

  public PointsEventHandler(PointsLedgerMapper ledgers, AppUserMapper users, ObjectMapper json) {
    this.ledgers = ledgers;
    this.users = users;
    this.json = json;
  }

  @Transactional
  public void handle(String payload) {
    try {
      CheckinService.PointsEvent event = json.readValue(payload, CheckinService.PointsEvent.class);
      if (ledgers.selectCount(
              new LambdaQueryWrapper<PointsLedger>().eq(PointsLedger::getEventId, event.eventId()))
          > 0) return;
      AppUser user = users.selectById(event.userId());
      if (user == null) throw new IllegalStateException("user not found");
      PointsLedger ledger = new PointsLedger();
      ledger.setEventId(event.eventId());
      ledger.setUserId(event.userId());
      ledger.setMeetingId(event.meetingId());
      ledger.setDelta(event.delta());
      ledger.setReason(event.reason());
      ledger.setCreatedAt(LocalDateTime.now());
      ledgers.insert(ledger);
      user.setPointsBalance(user.getPointsBalance() + event.delta());
      user.setUpdatedAt(LocalDateTime.now());
      users.updateById(user);
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalArgumentException("invalid points event", ex);
    }
  }
}
