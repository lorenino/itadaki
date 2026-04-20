package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.MealPhoto;
import fr.esgi.hla.itadaki.dto.meal.MealPhotoResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MapStruct mapper between MealPhoto entity and photo-related DTOs.
 *
 * Field alignment (entity → DTO):
 *   storagePath    → photoUrl  (raw path; URL building with base URL is done in Step 3 via FileStorageService)
 *   originalFileName → originalFileName  (identical names — no @Mapping needed)
 *   contentType    → contentType  (identical names)
 *   size           → size       (identical names)
 *   uploadedAt     → uploadedAt (LocalDateTime → String via @Named)
 *
 * TODO: Step 3 — replace storagePath-passthrough with FileStorageService.getFileUrl(storagePath).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealPhotoMapper {

    @Mapping(source = "storagePath", target = "photoUrl")
    @Mapping(source = "uploadedAt", target = "uploadedAt", qualifiedByName = "formatDateTime")
    MealPhotoResponseDto toDto(MealPhoto photo);

    @Mapping(source = "photoUrl", target = "storagePath")
    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    MealPhoto toEntity(MealPhotoResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    MealPhoto partialUpdate(MealPhotoResponseDto dto, @MappingTarget MealPhoto photo);

    List<MealPhotoResponseDto> toDto(List<MealPhoto> photos);

    List<MealPhoto> toEntity(List<MealPhotoResponseDto> dtos);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }
}
