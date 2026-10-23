package com.xmh.checkin.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  public static final String EXCHANGE = "checkin.events",
      QUEUE = "checkin.points",
      ROUTING = "points.award";

  @Bean
  DirectExchange checkinExchange() {
    return new DirectExchange(EXCHANGE, true, false);
  }

  @Bean
  Queue pointsQueue() {
    return QueueBuilder.durable(QUEUE).build();
  }

  @Bean
  Binding pointsBinding(Queue pointsQueue, DirectExchange checkinExchange) {
    return BindingBuilder.bind(pointsQueue).to(checkinExchange).with(ROUTING);
  }
}
