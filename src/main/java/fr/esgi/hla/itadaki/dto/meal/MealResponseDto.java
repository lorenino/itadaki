package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * DTO for returning a full meal record.
 * TODO: Map from Meal entity via MealMapper.
 * TODO: Include nested MealPhotoResponseDto for the photo details.
 */
public record MealResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String status,
        MealPhotoResponseDto photo,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long userId
) implements Serializable {
}
