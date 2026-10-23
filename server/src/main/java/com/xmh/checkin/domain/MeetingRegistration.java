package com.xmh.checkin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("meeting_registration")
public class MeetingRegistration {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long meetingId;
  private Long userId;
  private String status;
  private LocalDateTime registeredAt;
}
