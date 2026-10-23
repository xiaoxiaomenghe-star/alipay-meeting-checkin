package com.xmh.checkin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("points_ledger")
public class PointsLedger {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String eventId;
  private Long userId;
  private Long meetingId;
  private Integer delta;
  private String reason;
  private LocalDateTime createdAt;
}
