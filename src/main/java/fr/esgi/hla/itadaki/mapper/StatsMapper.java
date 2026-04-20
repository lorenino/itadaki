package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for statistics DTOs.
 *
 * Stats are assembled from aggregation queries in StatsServiceImpl, not mapped from a single entity.
 * Concrete mapping methods will be added in Step 3 once the service aggregation strategy is defined.
 *
 * TODO: Step 3 — add projection-to-DTO mapping methods once aggregation approach is chosen
 *       (e.g., JPQL projection, raw query tuple, or manual construction in service).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatsMapper {

    // TODO: Step 3 — DailyCaloriesDto from aggregation projection or manual service construction
    // TODO: Step 3 — StatsOverviewDto assembled manually in StatsServiceImpl from multiple repository queries
}
