package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/** DTO for returning meal photo metadata — no binary data, storagePath-derived photoUrl only. */
public record MealPhotoResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String photoUrl,
        String originalFileName,
        String contentType,
        Long size,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt
) implements Serializable {
}
