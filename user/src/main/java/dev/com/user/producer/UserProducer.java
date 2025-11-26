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

    public void sendMenssage(UserEntity user) {
        var emailResponse = new EmailResponse();
        emailResponse.setUserId(user.getUserId());
        emailResponse.setEmailTo(user.getEmail());
        emailResponse.setEmailSubject("Assunto do email: ");
        emailResponse.setEmailBody("Whello, " + user.getName() + " bem vindo a plataforma.");
        template.convertAndSend(RabbitMq.EXCHANGE_NAME, "email.key", emailResponse);
    }


}
