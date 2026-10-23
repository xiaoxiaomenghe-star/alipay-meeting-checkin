package com.xmh.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xmh.checkin.api.BusinessException;
import com.xmh.checkin.domain.AppUser;
import com.xmh.checkin.domain.PointsLedger;
import com.xmh.checkin.mapper.AppUserMapper;
import com.xmh.checkin.mapper.PointsLedgerMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PointsService {
  private final AppUserMapper users;
  private final PointsLedgerMapper ledgers;

  public PointsService(AppUserMapper users, PointsLedgerMapper ledgers) {
    this.users = users;
    this.ledgers = ledgers;
  }

  public int balance(long userId) {
    AppUser u = users.selectById(userId);
    if (u == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在", HttpStatus.NOT_FOUND);
    return u.getPointsBalance();
  }

  public List<PointsLedger> ledger(long userId) {
    return ledgers.selectList(
        new LambdaQueryWrapper<PointsLedger>()
            .eq(PointsLedger::getUserId, userId)
            .orderByDesc(PointsLedger::getCreatedAt));
  }
}
