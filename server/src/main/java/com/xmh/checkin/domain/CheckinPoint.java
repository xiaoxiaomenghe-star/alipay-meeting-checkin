package com.xmh.checkin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("checkin_point")
public class CheckinPoint {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long meetingId;
  private String pointCode;
  private String pointName;
  private BigDecimal latitude;
  private BigDecimal longitude;
  private Integer allowedDistanceMeters;
  private String status;
}
