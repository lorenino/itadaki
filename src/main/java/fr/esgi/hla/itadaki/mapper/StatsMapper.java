package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for statistics DTOs.
 * Stats are typically built from aggregation projections, not single entities.
 * TODO: Add custom @Named methods to map from repository projection interfaces
 *       (e.g., DailyStatsProjection) to DailyCaloriesDto.
 * TODO: StatsOverviewDto may be assembled manually in StatsServiceImpl from multiple queries.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatsMapper {

    // TODO: DailyCaloriesDto toDto(DailyStatsProjection projection);
    // TODO: List<DailyCaloriesDto> toDto(List<DailyStatsProjection> projections);
    // TODO: StatsOverviewDto toOverviewDto(...);
}
