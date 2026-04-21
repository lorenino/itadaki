package fr.esgi.hla.itadaki.dto.correction;

import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/** DTO for submitting a user correction on a meal analysis. */
public record MealCorrectionRequestDto(
        String correctedDishName,
        @NotEmpty(message = "Corrected items list must not be empty") List<CorrectedFoodItemDto> correctedItems,
        Double correctedTotalCalories,
        String userComment
) implements Serializable {
}
