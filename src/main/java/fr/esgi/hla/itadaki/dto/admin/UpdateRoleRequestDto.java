package fr.esgi.hla.itadaki.dto.admin;

import fr.esgi.hla.itadaki.business.enums.UserRole;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Requete admin : promouvoir/retrograder un utilisateur.
 */
public record UpdateRoleRequestDto(
        @NotNull(message = "Role is required") UserRole role
) implements Serializable {
}
