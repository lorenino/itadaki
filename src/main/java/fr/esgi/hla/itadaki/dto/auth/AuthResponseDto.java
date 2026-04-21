package fr.esgi.hla.itadaki.dto.auth;

import fr.esgi.hla.itadaki.dto.user.UserResponseDto;

import java.io.Serializable;

/** DTO returned after successful authentication (login or register). */
public record AuthResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponseDto user
) implements Serializable {
}
