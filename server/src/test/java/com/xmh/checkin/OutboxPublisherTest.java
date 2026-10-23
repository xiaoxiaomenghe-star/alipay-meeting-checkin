package com.xmh.checkin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.xmh.checkin.domain.OutboxEvent;
import com.xmh.checkin.mapper.OutboxEventMapper;
import com.xmh.checkin.service.OutboxPublisher;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class OutboxPublisherTest {
  @Test
  void failedPublishMovesEventToRetry() {
    OutboxEventMapper mapper = mock(OutboxEventMapper.class);
    RabbitTemplate rabbit = mock(RabbitTemplate.class);
    OutboxEvent event = event();
    when(mapper.selectList(any())).thenReturn(List.of(event));
    doThrow(new IllegalStateException("broker unavailable"))
        .when(rabbit)
        .convertAndSend(
            anyString(),
            anyString(),
            any(),
            any(org.springframework.amqp.core.MessagePostProcessor.class));
    new OutboxPublisher(mapper, rabbit).publishPending();
    assertThat(event.getStatus()).isEqualTo("RETRY");
    assertThat(event.getRetryCount()).isEqualTo(1);
    assertThat(event.getNextRetryAt()).isAfter(LocalDateTime.now());
    verify(mapper).updateById(event);
  }

  @Test
  void successfulPublishMarksEventSent() {
    OutboxEventMapper mapper = mock(OutboxEventMapper.class);
    RabbitTemplate rabbit = mock(RabbitTemplate.class);
    OutboxEvent event = event();
    when(mapper.selectList(any())).thenReturn(List.of(event));
    new OutboxPublisher(mapper, rabbit).publishPending();
    assertThat(event.getStatus()).isEqualTo("SENT");
    assertThat(event.getSentAt()).isNotNull();
    verify(mapper).updateById(event);
  }

  private OutboxEvent event() {
    OutboxEvent e = new OutboxEvent();
    e.setId(1L);
    e.setEventId("evt-1");
    e.setPayload("{}");
    e.setStatus("PENDING");
    e.setRetryCount(0);
    e.setNextRetryAt(LocalDateTime.now().minusSeconds(1));
    return e;
  }
}
