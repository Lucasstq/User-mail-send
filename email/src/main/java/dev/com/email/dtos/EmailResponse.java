package dev.com.email.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record EmailResponse(
        UUID userId,
        String emailFrom,
        String emailTo,
        String emailSubject,
        String body
) {
}

