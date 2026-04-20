package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Compact DTO used in paginated meal history list responses.
 * Avoids loading full analysis details — carries only display-ready summary fields.
 * TODO: Step 3 — populated via MealMapper.toHistoryItemDto(Meal).
 *       photoUrl built from meal.photo.storagePath via FileStorageService.
 *       detectedDishName and totalCalories sourced from meal.analysis.
 */
public record MealHistoryItemDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String status,
        String photoUrl,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt,
        String detectedDishName,
        Double totalCalories
) implements Serializable {
}
