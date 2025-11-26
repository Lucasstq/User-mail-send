package dev.com.user.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserRequest
        (@NotBlank(message = "nome não pode ser nulo")
         String name,
         @Email(message = "digite um email válido")
         String email) {
}
