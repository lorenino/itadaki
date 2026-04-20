package fr.esgi.hla.itadaki.dto.meal;

import fr.esgi.hla.itadaki.business.enums.MealType;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Body pour PATCH /api/meals/{id}/type.
 * Permet a l'utilisateur de re-classer son repas (petit-dej / dej / snack / diner)
 * apres que l'auto-detection par heure ait suggere une categorie.
 */
public record UpdateMealTypeRequestDto(
        @NotNull(message = "mealType is required") MealType mealType
) implements Serializable {
}
