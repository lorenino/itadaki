package fr.esgi.hla.itadaki.dto.auth;

import fr.esgi.hla.itadaki.dto.user.UserResponseDto;

import java.io.Serializable;

/**
 * DTO returned after successful authentication (login or register).
 * TODO: Populate tokenType always as "Bearer".
 * TODO: Populate expiresIn from app.jwt.expiration-ms property.
 */
public record AuthResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponseDto user
) implements Serializable {
}
