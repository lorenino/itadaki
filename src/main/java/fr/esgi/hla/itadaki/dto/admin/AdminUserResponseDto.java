package fr.esgi.hla.itadaki.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.esgi.hla.itadaki.business.enums.UserRole;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO admin : vue enrichie d'un utilisateur avec son nombre de repas.
 */
public record AdminUserResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String username,
        String email,
        UserRole role,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) LocalDateTime createdAt,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) long mealCount
) implements Serializable {
}
