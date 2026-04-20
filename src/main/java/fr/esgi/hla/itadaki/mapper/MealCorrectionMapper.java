package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.MealCorrection;
import fr.esgi.hla.itadaki.dto.correction.MealCorrectionResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper between MealCorrection entity and correction-related DTOs.
 * TODO: Add @Mapping(source = "meal.id", target = "mealId").
 * TODO: Add @Mapping(source = "correctedAt", target = "correctedAt") with ISO-8601 formatting.
 * TODO: correctedItems — may require custom @Named method to map from/to entity storage format.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealCorrectionMapper {

    MealCorrectionResponseDto toDto(MealCorrection correction);

    MealCorrection toEntity(MealCorrectionResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MealCorrection partialUpdate(MealCorrectionResponseDto dto, @MappingTarget MealCorrection correction);

    List<MealCorrectionResponseDto> toDto(List<MealCorrection> corrections);

    List<MealCorrection> toEntity(List<MealCorrectionResponseDto> dtos);
}
