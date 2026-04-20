package fr.esgi.hla.itadaki.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for user registration.
 * Username allows letters (including accents), digits and _.- separators
 * to avoid HTML/JS injection via profile display.
 */
public record RegisterRequestDto(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be between {min} and {max} characters")
        @Pattern(regexp = "^[\\p{L}\\p{N}_.-]+$",
                message = "Username: lettres, chiffres, _ . - uniquement")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Valid email required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least {min} characters")
        String password
) implements Serializable {
}
