package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.MealAnalysis;
import fr.esgi.hla.itadaki.dto.analysis.MealAnalysisResponseDto;
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

/** MapStruct mapper for MealAnalysis — detectedItems left null (service enriches from JSON). */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealAnalysisMapper {

    @Mapping(source = "meal.id", target = "mealId")
    @Mapping(target = "detectedItems", ignore = true)
    @Mapping(source = "analyzedAt", target = "analyzedAt", qualifiedByName = "formatDateTime")
    MealAnalysisResponseDto toDto(MealAnalysis analysis);

    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "detectedItemsJson", ignore = true)
    @Mapping(target = "rawModelResponse", ignore = true)
    MealAnalysis toEntity(MealAnalysisResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "detectedItemsJson", ignore = true)
    @Mapping(target = "rawModelResponse", ignore = true)
    @Mapping(target = "analyzedAt", ignore = true)
    MealAnalysis partialUpdate(MealAnalysisResponseDto dto, @MappingTarget MealAnalysis analysis);

    List<MealAnalysisResponseDto> toDto(List<MealAnalysis> analyses);

    List<MealAnalysis> toEntity(List<MealAnalysisResponseDto> dtos);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }
}
