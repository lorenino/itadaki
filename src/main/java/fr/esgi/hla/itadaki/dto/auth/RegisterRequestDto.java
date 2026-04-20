package fr.esgi.hla.itadaki.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for user registration.
 * TODO: Add custom @ValidPassword annotation on password field.
 * TODO: Add cross-field password confirmation validation.
 */
public record RegisterRequestDto(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Email is required") @Email(message = "Valid email required") String email,
        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least {min} characters") String password
) implements Serializable {
}
