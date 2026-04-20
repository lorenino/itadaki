package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.MealPhoto;
import fr.esgi.hla.itadaki.dto.meal.MealPhotoResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper between MealPhoto entity and photo-related DTOs.
 * TODO: Add @Mapping(source = "storedPath", target = "photoUrl") with a custom
 *       @Named method that calls FileStorageService.getFileUrl(storedPath).
 * TODO: Add @Mapping(source = "uploadedAt", target = "uploadedAt") with ISO-8601 formatting.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealPhotoMapper {

    MealPhotoResponseDto toDto(MealPhoto photo);

    MealPhoto toEntity(MealPhotoResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MealPhoto partialUpdate(MealPhotoResponseDto dto, @MappingTarget MealPhoto photo);

    List<MealPhotoResponseDto> toDto(List<MealPhoto> photos);

    List<MealPhoto> toEntity(List<MealPhotoResponseDto> dtos);
}
