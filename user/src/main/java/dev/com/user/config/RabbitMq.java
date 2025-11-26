package dev.com.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {

    /*
    A exchange é um roteador: ela não guarda mensagens;
    ela recebe e decide para qual(s) fila(s) repassar a mensagem.
     */
    public static final String EXCHANGE_NAME = "user_created";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper(); //consome Json como Objeto
        return new Jackson2JsonMessageConverter(objectMapper);
    }

}
