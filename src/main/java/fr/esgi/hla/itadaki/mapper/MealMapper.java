package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.dto.meal.MealHistoryItemDto;
import fr.esgi.hla.itadaki.dto.meal.MealResponseDto;
import fr.esgi.hla.itadaki.dto.meal.MealUploadResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper between Meal entity and meal-related DTOs.
 * TODO: Add @Mapping(source = "user.id", target = "userId").
 * TODO: Add @Mapping(source = "photo", target = "photo") for nested MealPhotoResponseDto.
 * TODO: Add @Mapping(source = "analysis.totalCalories", target = "totalCalories") for history item.
 * TODO: Add @Mapping(source = "photo.storedPath", target = "photoUrl") with custom method for URL building.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealMapper {

    MealResponseDto toDto(Meal meal);

    MealHistoryItemDto toHistoryItemDto(Meal meal);

    MealUploadResponseDto toUploadResponseDto(Meal meal);

    Meal toEntity(MealResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Meal partialUpdate(MealResponseDto dto, @MappingTarget Meal meal);

    List<MealResponseDto> toDto(List<Meal> meals);

    List<MealHistoryItemDto> toHistoryItemDto(List<Meal> meals);
}
