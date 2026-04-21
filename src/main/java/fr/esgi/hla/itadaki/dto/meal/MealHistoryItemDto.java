package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/** Compact DTO for paginated meal history — summary fields only (no full analysis). */
public record MealHistoryItemDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String status,
        String mealType,
        String photoUrl,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt,
        String detectedDishName,
        Double totalCalories
) implements Serializable {
}
