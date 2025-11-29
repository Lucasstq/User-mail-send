package dev.com.user.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserUpdateRequest(@NotBlank(message = "Nome não pode ser nulo.") String name) {
}
