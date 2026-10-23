package com.xmh.checkin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("app_user")
public class AppUser {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String externalId;
  private String displayName;
  private Integer pointsBalance;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
