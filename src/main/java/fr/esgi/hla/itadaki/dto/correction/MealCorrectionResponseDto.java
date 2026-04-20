package fr.esgi.hla.itadaki.dto.correction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for returning a meal correction record.
 * TODO: Step 3 — populated via MealCorrectionMapper.toDto(MealCorrection).
 *       correctedItems parsed from MealCorrection.correctedItemsJson via ObjectMapper in CorrectionServiceImpl.
 */
public record MealCorrectionResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long mealId,
        String correctedDishName,
        List<CorrectedFoodItemDto> correctedItems,
        Double correctedTotalCalories,
        String userComment,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String correctedAt
) implements Serializable {
}
