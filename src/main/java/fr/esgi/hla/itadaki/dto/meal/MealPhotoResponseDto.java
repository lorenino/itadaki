package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * DTO for returning meal photo metadata in API responses.
 * TODO: Map from MealPhoto entity via MealPhotoMapper.
 * TODO: photoUrl is the public-facing URL built from storedPath + base URL (via FileStorageService).
 */
public record MealPhotoResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String photoUrl,
        String originalFilename,
        String mimeType,
        Long fileSizeBytes,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt
) implements Serializable {
}
