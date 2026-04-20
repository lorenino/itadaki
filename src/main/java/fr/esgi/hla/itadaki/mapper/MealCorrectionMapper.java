package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.MealCorrection;
import fr.esgi.hla.itadaki.dto.correction.MealCorrectionResponseDto;
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
 * MapStruct mapper between MealCorrection entity and correction-related DTOs.
 *
 * correctedItems is intentionally ignored here — MealCorrection stores a JSON string
 * (correctedItemsJson) that requires ObjectMapper parsing. The service enriches
 * the DTO after mapping by constructing a new record:
 *   new MealCorrectionResponseDto(..., parsedItems, ...)
 *
 * TODO: Step 3 — CorrectionServiceImpl enriches correctedItems from correctedItemsJson via ObjectMapper.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealCorrectionMapper {

    @Mapping(source = "meal.id", target = "mealId")
    @Mapping(target = "correctedItems", ignore = true)
    @Mapping(source = "correctedAt", target = "correctedAt", qualifiedByName = "formatDateTime")
    MealCorrectionResponseDto toDto(MealCorrection correction);

    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "correctedItemsJson", ignore = true)
    MealCorrection toEntity(MealCorrectionResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "correctedItemsJson", ignore = true)
    @Mapping(target = "correctedAt", ignore = true)
    MealCorrection partialUpdate(MealCorrectionResponseDto dto, @MappingTarget MealCorrection correction);

    List<MealCorrectionResponseDto> toDto(List<MealCorrection> corrections);

    List<MealCorrection> toEntity(List<MealCorrectionResponseDto> dtos);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }
}
