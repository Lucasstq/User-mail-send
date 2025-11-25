package dev.com.email.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {

    public static final String QUEUE_NAME = "email_queue";
    public static final String EXCHANGE_NAME = "send_exchange";
    public static final String ROUTING_KEY = "email.key";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding() {
        return  BindingBuilder
                .bind(queue())  //vincular a essa fila
                .to(exchange()) //para essa exchange
                .with(ROUTING_KEY); //usando essa rota
    }

}
