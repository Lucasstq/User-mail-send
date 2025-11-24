package dev.com.email.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record EmailResponse(
        UUID id,
        String subject,
        String body
) {
}

