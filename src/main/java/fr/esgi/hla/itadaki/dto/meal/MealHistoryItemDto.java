package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Compact DTO used in meal history list responses.
 * Avoids loading full analysis details in list views.
 * TODO: Map from Meal entity via MealMapper — include photo URL and summary calories.
 */
public record MealHistoryItemDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String status,
        String photoUrl,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt,
        Double totalCalories
) implements Serializable {
}
