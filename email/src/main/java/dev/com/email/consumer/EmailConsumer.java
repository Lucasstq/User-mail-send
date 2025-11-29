package dev.com.email.consumer;

import dev.com.email.configuration.RabbitMq;
import dev.com.email.entities.EmailEntity;
import dev.com.email.mapper.EmailMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @RabbitListener(queues = RabbitMq.QUEUE_NAME)
    public void listenEmailQueue(@Payload EmailEntity entity) {
        EmailMapper.toResponse(entity);
    }
}
