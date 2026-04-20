package fr.esgi.hla.itadaki.dto.correction;

import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for submitting a user correction on a meal analysis.
 * TODO: Validated at controller level with @Valid.
 * TODO: MealCorrectionValidator called in CorrectionServiceImpl for business-level checks.
 */
public record MealCorrectionRequestDto(
        @NotEmpty(message = "Corrected items list must not be empty") List<CorrectedFoodItemDto> correctedItems,
        String note
) implements Serializable {
}
