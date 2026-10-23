package com.xmh.checkin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("outbox_event")
public class OutboxEvent {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String eventId;
  private String aggregateType;
  private String aggregateId;
  private String eventType;
  private String payload;
  private String status;
  private Integer retryCount;
  private LocalDateTime nextRetryAt;
  private LocalDateTime createdAt;
  private LocalDateTime sentAt;
}
