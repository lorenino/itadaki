package fr.esgi.hla.itadaki.dto.meal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * DTO for returning meal photo metadata in API responses.
 * No binary data — storagePath-derived photoUrl only.
 * TODO: Step 3 — populated via MealPhotoMapper.toDto(MealPhoto).
 *       photoUrl built from storagePath via FileStorageService.getFileUrl().
 */
public record MealPhotoResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String photoUrl,
        String originalFileName,
        String contentType,
        Long size,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) String uploadedAt
) implements Serializable {
}
