package fr.esgi.hla.itadaki.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * DTO for user login.
 * TODO: Validated at controller level with @Valid.
 */
public record LoginRequestDto(
        @NotBlank(message = "Email is required") @Email(message = "Valid email required") String email,
        @NotBlank(message = "Password is required") String password
) implements Serializable {
}
