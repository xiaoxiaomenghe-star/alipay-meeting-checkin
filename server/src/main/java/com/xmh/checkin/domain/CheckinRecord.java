package com.xmh.checkin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("checkin_record")
public class CheckinRecord {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long meetingId;
  private Long userId;
  private Long pointId;
  private String checkinType;
  private BigDecimal latitude;
  private BigDecimal longitude;
  private BigDecimal distanceMeters;
  private LocalDateTime checkedInAt;
  private String traceId;
}
