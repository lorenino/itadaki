package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.dto.meal.MealHistoryItemDto;
import fr.esgi.hla.itadaki.dto.meal.MealResponseDto;
import fr.esgi.hla.itadaki.dto.meal.MealUploadResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** MapStruct mapper between Meal entity and meal-related DTOs. Uses MealPhotoMapper for photo mapping. */
@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MealPhotoMapper.class}
)
public interface MealMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "uploadedAt", target = "uploadedAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "formatDateTime")
    MealResponseDto toDto(Meal meal);

    @Mapping(source = "id", target = "mealId")
    @Mapping(source = "photo.id", target = "photoId")
    @Mapping(source = "uploadedAt", target = "uploadedAt", qualifiedByName = "formatDateTime")
    MealUploadResponseDto toUploadResponseDto(Meal meal);

    @Mapping(source = "photo.storagePath", target = "photoUrl")
    @Mapping(source = "uploadedAt", target = "uploadedAt", qualifiedByName = "formatDateTime")
    @Mapping(source = "analysis.detectedDishName", target = "detectedDishName")
    @Mapping(source = "analysis.estimatedTotalCalories", target = "totalCalories")
    MealHistoryItemDto toHistoryItemDto(Meal meal);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "analysis", ignore = true)
    @Mapping(target = "correction", ignore = true)
    Meal toEntity(MealResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "analysis", ignore = true)
    @Mapping(target = "correction", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mealType", ignore = true)
    Meal partialUpdate(MealResponseDto dto, @MappingTarget Meal meal);

    List<MealResponseDto> toDto(List<Meal> meals);

    List<MealHistoryItemDto> toHistoryItemDto(List<Meal> meals);
}
