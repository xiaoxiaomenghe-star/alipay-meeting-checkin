package com.xmh.checkin.service;

import com.xmh.checkin.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PointsConsumer {
  private final PointsEventHandler handler;

  public PointsConsumer(PointsEventHandler handler) {
    this.handler = handler;
  }

  @RabbitListener(queues = RabbitConfig.QUEUE)
  public void consume(String payload) {
    handler.handle(payload);
  }
}
