package dev.com.email.consumer;

import dev.com.email.configuration.RabbitMq;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @RabbitListener(queues = RabbitMq.QUEUE_NAME)
    public void listenEmailQueue(@Payload String s) {
        System.out.println(s);
    }
}
