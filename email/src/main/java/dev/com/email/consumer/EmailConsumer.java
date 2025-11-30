package dev.com.email.consumer;

import dev.com.email.configuration.RabbitMq;
import dev.com.email.entities.EmailEntity;
import dev.com.email.mapper.EmailMapper;
import dev.com.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService service;

    @RabbitListener(queues = RabbitMq.QUEUE_NAME)
    public void listenEmailQueue(@Payload EmailEntity entity) {
        EmailMapper.toResponse(entity);
        service.sendEmail(entity);
    }
}
