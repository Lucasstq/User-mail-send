package dev.com.user.dtos.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponse(UUID userId, String name, String email) {
}
