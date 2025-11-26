package dev.com.email.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {

    public static final String QUEUE_NAME = "email_queue";
    public static final String EXCHANGE_NAME = "user_created";
    public static final String BINDING_KEY = "email.key";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    //converte Json em objeto
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper(); //consome Json como Objeto
        return new Jackson2JsonMessageConverter(objectMapper);
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
                .with(BINDING_KEY); //usando essa rota
    }

}
