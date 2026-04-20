package fr.esgi.hla.itadaki.dto.correction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for returning a meal correction record.
 * TODO: Map from MealCorrection entity via MealCorrectionMapper.
 */
public record MealCorrectionResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long mealId,
        List<CorrectedFoodItemDto> correctedItems,
        Double correctedCalories,
        Double correctedProtein,
        Double correctedCarbs,
        Double correctedFat,
        String note,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String correctedAt
) implements Serializable {
}
