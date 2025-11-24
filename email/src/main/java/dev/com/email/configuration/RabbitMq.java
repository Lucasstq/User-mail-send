package dev.com.email.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {

    public static final String QUEUE_NAME = "email_queue";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

}
