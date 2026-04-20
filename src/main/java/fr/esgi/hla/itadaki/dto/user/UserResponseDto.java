package fr.esgi.hla.itadaki.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * DTO for returning user profile data in API responses.
 * Never expose passwordHash through this DTO.
 * TODO: Map from User entity via UserMapper.
 */
public record UserResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String username,
        String email,
        String role,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String createdAt
) implements Serializable {
}
