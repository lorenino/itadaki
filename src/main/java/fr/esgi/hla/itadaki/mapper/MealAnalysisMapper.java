package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.MealAnalysis;
import fr.esgi.hla.itadaki.dto.analysis.MealAnalysisResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper between MealAnalysis entity and analysis-related DTOs.
 * TODO: Add @Mapping(source = "meal.id", target = "mealId").
 * TODO: Add @Mapping(source = "analyzedAt", target = "analyzedAt") with ISO-8601 formatting.
 * TODO: detectedItems — parsed from rawAiResponse; may require a @Named custom method.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealAnalysisMapper {

    MealAnalysisResponseDto toDto(MealAnalysis analysis);

    MealAnalysis toEntity(MealAnalysisResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MealAnalysis partialUpdate(MealAnalysisResponseDto dto, @MappingTarget MealAnalysis analysis);

    List<MealAnalysisResponseDto> toDto(List<MealAnalysis> analyses);

    List<MealAnalysis> toEntity(List<MealAnalysisResponseDto> dtos);
}
