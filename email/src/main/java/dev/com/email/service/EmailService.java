package dev.com.email.service;

import dev.com.email.entities.EmailEntity;
import dev.com.email.enums.EmailStatus;
import dev.com.email.repositories.EmailRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    @Value("${EMAIL_USERNAME}")
    private String emailFrom;

    @Transactional
    public void sendEmail(EmailEntity entity) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(entity.getEmailTo());
            message.setSubject(entity.getEmailSubject());
            message.setText(entity.getEmailBody());
            mailSender.send(message);
            entity.setStatusEmail(EmailStatus.SENT);
        } catch (Exception e) {
            entity.setStatusEmail(EmailStatus.FAILED);
            System.out.println("Error sending email: " + e.getMessage());
        }
        emailRepository.save(entity);
    }

}
