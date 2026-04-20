package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Lightweight DTO returned immediately after a meal image is uploaded.
 * Contains just enough for the client to poll for analysis status.
 * TODO: Map from Meal + MealPhoto entities via MealMapper.
 */
public record MealUploadResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long mealId,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long photoId,
        String status,
        String message
) implements Serializable {
}
