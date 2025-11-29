package dev.com.email.mapper;

import dev.com.email.dtos.EmailResponse;
import dev.com.email.entities.EmailEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailMapper {

    public static EmailResponse toResponse(EmailEntity entity) {
        return EmailResponse
                .builder()
                .emailTo(entity.getEmailTo())
                .emailFrom(entity.getEmailFrom())
                .emailSubject(entity.getEmailSubject())
                .emailBody(entity.getEmailBody())
                .userId(entity.getUserId())
                .statusEmail(entity.getStatusEmail())
                .build();
    }


}
