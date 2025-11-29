package dev.com.email.dtos;

import dev.com.email.enums.EmailStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record EmailResponse(
        UUID userId,
        String emailFrom,
        String emailTo,
        String emailSubject,
        String emailBody,
        EmailStatus statusEmail
) {
}

