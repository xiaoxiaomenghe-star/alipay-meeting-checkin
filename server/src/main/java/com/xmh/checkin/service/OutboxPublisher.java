package com.xmh.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xmh.checkin.config.RabbitConfig;
import com.xmh.checkin.domain.OutboxEvent;
import com.xmh.checkin.mapper.OutboxEventMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.outbox-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
  private final OutboxEventMapper outbox;
  private final RabbitTemplate rabbit;

  public OutboxPublisher(OutboxEventMapper outbox, RabbitTemplate rabbit) {
    this.outbox = outbox;
    this.rabbit = rabbit;
  }

  @Scheduled(fixedDelay = 1000)
  public void publishPending() {
    List<OutboxEvent> events =
        outbox.selectList(
            new LambdaQueryWrapper<OutboxEvent>()
                .in(OutboxEvent::getStatus, "PENDING", "RETRY")
                .le(OutboxEvent::getNextRetryAt, LocalDateTime.now())
                .orderByAsc(OutboxEvent::getId)
                .last("LIMIT 50"));
    for (OutboxEvent event : events) {
      try {
        rabbit.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ROUTING,
            event.getPayload(),
            m -> {
              m.getMessageProperties().setMessageId(event.getEventId());
              return m;
            });
        event.setStatus("SENT");
        event.setSentAt(LocalDateTime.now());
        outbox.updateById(event);
      } catch (Exception ex) {
        event.setStatus("RETRY");
        event.setRetryCount(event.getRetryCount() + 1);
        event.setNextRetryAt(
            LocalDateTime.now()
                .plusSeconds(Math.min(60, 1L << Math.min(5, event.getRetryCount()))));
        outbox.updateById(event);
      }
    }
  }
}
