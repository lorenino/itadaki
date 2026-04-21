package fr.esgi.hla.itadaki.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.esgi.hla.itadaki.dto.meal.DetectedFoodItemDto;

import java.io.Serializable;
import java.util.List;

/** DTO for returning the AI analysis result of a meal. */
public record MealAnalysisResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long mealId,
        String detectedDishName,
        List<DetectedFoodItemDto> detectedItems,
        Double estimatedTotalCalories,
        Double confidenceScore,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String rawModelResponse,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String analyzedAt
) implements Serializable {
}
