package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/** DTO for returning a full meal record, including its associated photo. */
public record MealResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String status,
        String mealType,
        MealPhotoResponseDto photo,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String updatedAt,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long userId
) implements Serializable {
}
