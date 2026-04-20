package fr.esgi.hla.itadaki.dto.correction;

import java.io.Serializable;

/**
 * DTO for a single food item as corrected by the user.
 * TODO: Validated inside MealCorrectionRequestDto list via @Valid @NotEmpty.
 */
public record CorrectedFoodItemDto(
        String name,
        Double quantity,
        String unit,
        Double calories,
        Double protein,
        Double carbs,
        Double fat
) implements Serializable {
}
