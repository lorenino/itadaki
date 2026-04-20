package fr.esgi.hla.itadaki.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.esgi.hla.itadaki.dto.meal.DetectedFoodItemDto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for returning the full AI analysis result of a meal.
 * TODO: Map from MealAnalysis entity via MealAnalysisMapper.
 * TODO: detectedItems parsed from rawAiResponse by OllamaService.
 */
public record MealAnalysisResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long mealId,
        List<DetectedFoodItemDto> detectedItems,
        Double totalCalories,
        Double totalProtein,
        Double totalCarbs,
        Double totalFat,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String analyzedAt
) implements Serializable {
}
