package com.xmh.checkin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("meeting")
public class Meeting {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String title;
  private String description;
  private String status;
  private LocalDateTime registrationStart;
  private LocalDateTime registrationEnd;
  private LocalDateTime checkinStart;
  private LocalDateTime checkinEnd;
  private Integer pointsReward;
  private LocalDateTime createdAt;
}
