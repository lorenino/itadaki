package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/** Lightweight DTO returned immediately after a meal image is uploaded. */
public record MealUploadResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long mealId,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long photoId,
        String status,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt
) implements Serializable {
}
