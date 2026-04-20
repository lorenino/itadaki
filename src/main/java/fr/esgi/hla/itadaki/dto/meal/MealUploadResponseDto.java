package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Lightweight DTO returned immediately after a meal image is uploaded.
 * Contains just enough for the client to know the meal and photo IDs and poll for analysis status.
 * TODO: Step 3 — populated via MealMapper.toUploadResponseDto(Meal).
 */
public record MealUploadResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long mealId,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long photoId,
        String status,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt
) implements Serializable {
}
