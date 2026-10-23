package com.xmh.checkin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.xmh.checkin.mapper")
@SpringBootApplication
public class MeetingCheckinApplication {
  public static void main(String[] args) {
    SpringApplication.run(MeetingCheckinApplication.class, args);
  }
}
