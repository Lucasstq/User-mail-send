package dev.com.user.producer;

import dev.com.user.config.RabbitMq;
import dev.com.user.dtos.response.EmailResponse;
import dev.com.user.entities.UserEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserProducer {

    private final RabbitTemplate template;

    public UserProducer(RabbitTemplate template) {
        this.template = template;
    }

    /*
    Nesse metodo eu to usando uma routingKey que é a mesma da minha BINDING_KEY
    ou seja, eu to usando uma Exchange direct que roteia
    pela igualdade exata entre routing key e binding key.
     */
    public void sendMenssage(UserEntity user) {
        var emailResponse = new EmailResponse();
        emailResponse.setUserId(user.getUserId());
        emailResponse.setEmailTo(user.getEmail());
        emailResponse.setEmailSubject("Assunto do email: ");
        emailResponse.setEmailBody("Whello, " + user.getName() + " bem vindo a plataforma.");
        template.convertAndSend(RabbitMq.EXCHANGE_NAME, "email.key", emailResponse);
    }

    /*
    Producer publica na exchange, com aquela routingKey, binding procura uma BINDING_KEY
    com o mesmo nome (DIRECT EXCHANGE) e vai encontrar no nosso consumer a BINDING_KEY
    logo ela vai deixar a mensagem na fila para ser consumida.
     */


}
